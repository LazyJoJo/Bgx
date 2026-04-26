import FundEditModal from '@/components/funds/FundEditModal'
import fundsReducer from '@/store/slices/fundsSlice'
import { Fund } from '@/types'
import { configureStore } from '@reduxjs/toolkit'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { message } from 'antd'
import React from 'react'
import { Provider } from 'react-redux'
import { beforeEach, describe, expect, it, vi } from 'vitest'

// Use vi.hoisted to define mocks that reference each other
const { mockFetchAllFunds, mockDispatch } = vi.hoisted(() => ({
    mockFetchAllFunds: vi.fn(),
    mockDispatch: vi.fn()
}))

// Mock the fundsApi
vi.mock('@/services/api/funds', () => ({
    fundsApi: {
        updateFund: vi.fn()
    }
}))

// Mock message
vi.mock('antd', async () => {
    const actual = await vi.importActual('antd')
    return {
        ...actual,
        message: {
            success: vi.fn(),
            error: vi.fn(),
            info: vi.fn()
        }
    }
})

// Mock dispatch
vi.mock('@store/hooks', () => ({
    useAppDispatch: () => mockDispatch
}))

// Mock fundsSlice
vi.mock('@store/slices/fundsSlice', () => ({
    fetchAllFunds: mockFetchAllFunds,
    default: () => ({
        list: [],
        selectedFund: null,
        loading: false,
        error: null
    })
}))

const createTestStore = () => {
    return configureStore({
        reducer: {
            funds: fundsReducer
        }
    })
}

const mockFund: Fund = {
    id: 1,
    code: '001593',
    name: '天弘创业板ETF联接C',
    type: '指数基金',
    market: 'SZ',
    category: 'INDEX',
    description: '测试描述',
    collectionFrequency: 1440,
    dataSource: 'SINA',
    active: true
}

const TestWrapper: React.FC<{ children: React.ReactNode; store?: ReturnType<typeof createTestStore> }> = ({
    children,
    store = createTestStore()
}) => (
    <Provider store={store}>
        {children}
    </Provider>
)

describe('FundEditModal', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('Rendering', () => {
        it('should render modal with edit title when visible', () => {
            const onClose = vi.fn()
            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            expect(screen.getByText(/编辑基金/)).toBeTruthy()
        })

        it('should display read-only fund info (code, name, type)', () => {
            const onClose = vi.fn()
            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            // Check read-only fields are displayed
            expect(screen.getByText(/基金代码：/)).toBeTruthy()
            expect(screen.getByText('001593')).toBeTruthy()
            expect(screen.getByText(/基金名称：/)).toBeTruthy()
            expect(screen.getByText('天弘创业板ETF联接C')).toBeTruthy()
            expect(screen.getByText(/基金类型：/)).toBeTruthy()
            expect(screen.getByText('指数基金')).toBeTruthy()
        })

        it('should display all form fields', () => {
            const onClose = vi.fn()
            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            // Check form fields are present
            expect(screen.getByLabelText('交易市场')).toBeTruthy()
            expect(screen.getByLabelText('基金分类')).toBeTruthy()
            expect(screen.getByLabelText('采集频率')).toBeTruthy()
            expect(screen.getByLabelText('数据源')).toBeTruthy()
            expect(screen.getByLabelText('描述')).toBeTruthy()
        })
    })

    describe('Modal Behavior', () => {
        it('should not render when visible is false', () => {
            const onClose = vi.fn()
            const { container } = render(
                <TestWrapper>
                    <FundEditModal
                        visible={false}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            expect(container.innerHTML).toBe('')
        })

        it('should display fund name in title when fund has name', () => {
            const onClose = vi.fn()
            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            expect(screen.getByText(/天弘创业板ETF联接C/)).toBeTruthy()
        })

        it('should display fund code in title when fund has no name', () => {
            const onClose = vi.fn()
            const fundWithoutName: Fund = { ...mockFund, name: '' }
            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={fundWithoutName}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            expect(screen.getByText(/001593/)).toBeTruthy()
        })
    })

    describe('Form Interaction', () => {
        it('should have Cancel and Save buttons', () => {
            const onClose = vi.fn()
            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            expect(screen.getByRole('button', { name: /取消/ })).toBeTruthy()
            expect(screen.getByRole('button', { name: /保存/ })).toBeTruthy()
        })

        it('should call onClose when Cancel button is clicked', async () => {
            const user = userEvent.setup()
            const onClose = vi.fn()
            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            const cancelButton = screen.getByRole('button', { name: /取消/ })
            await user.click(cancelButton)

            expect(onClose).toHaveBeenCalled()
        })
    })

    describe('API Calls', () => {
        it('should call updateFund API with correct data on submit', async () => {
            const user = userEvent.setup()
            const onClose = vi.fn()
            const { fundsApi } = await import('@/services/api/funds')

                // Mock successful API response
                ; (fundsApi.updateFund as ReturnType<typeof vi.fn>).mockResolvedValue({ success: true })

            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            const saveButton = screen.getByRole('button', { name: /保存/ })
            await user.click(saveButton)

            // Wait for the async operation
            await waitFor(() => {
                expect(fundsApi.updateFund).toHaveBeenCalledWith(1, expect.objectContaining({
                    market: mockFund.market,
                    category: mockFund.category,
                    collectionFrequency: mockFund.collectionFrequency,
                    dataSource: mockFund.dataSource,
                    description: mockFund.description
                }))
            })
        })

        it('should close modal and call onSuccess on successful update', async () => {
            const user = userEvent.setup()
            const onClose = vi.fn()
            const onSuccess = vi.fn()
            const { fundsApi } = await import('@/services/api/funds')

                // Mock successful API response
                ; (fundsApi.updateFund as ReturnType<typeof vi.fn>).mockResolvedValue({ success: true })

            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                        onSuccess={onSuccess}
                    />
                </TestWrapper>
            )

            const saveButton = screen.getByRole('button', { name: /保存/ })
            await user.click(saveButton)

            await waitFor(() => {
                expect(onSuccess).toHaveBeenCalled()
                expect(onClose).toHaveBeenCalled()
                expect(message.success).toHaveBeenCalledWith('基金信息更新成功')
            })
        })

        it('should dispatch fetchAllFunds after successful update', async () => {
            const user = userEvent.setup()
            const onClose = vi.fn()
            const { fundsApi } = await import('@/services/api/funds')

                // Mock successful API response
                ; (fundsApi.updateFund as ReturnType<typeof vi.fn>).mockResolvedValue({ success: true })

            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            const saveButton = screen.getByRole('button', { name: /保存/ })
            await user.click(saveButton)

            await waitFor(() => {
                expect(mockDispatch).toHaveBeenCalledWith(mockFetchAllFunds())
            })
        })

        it('should display error message on failed update', async () => {
            const user = userEvent.setup()
            const onClose = vi.fn()
            const { fundsApi } = await import('@/services/api/funds')

                // Mock failed API response
                ; (fundsApi.updateFund as ReturnType<typeof vi.fn>).mockResolvedValue({
                    success: false,
                    message: '更新失败：基金不存在'
                })

            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            const saveButton = screen.getByRole('button', { name: /保存/ })
            await user.click(saveButton)

            await waitFor(() => {
                expect(message.error).toHaveBeenCalledWith('更新失败：基金不存在')
            })
        })

        it('should handle network error gracefully', async () => {
            const user = userEvent.setup()
            const onClose = vi.fn()
            const { fundsApi } = await import('@/services/api/funds')

                // Mock network error
                ; (fundsApi.updateFund as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('网络连接失败'))

            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            const saveButton = screen.getByRole('button', { name: /保存/ })
            await user.click(saveButton)

            await waitFor(() => {
                expect(message.error).toHaveBeenCalledWith('网络连接失败')
            })
        })

        it('should show error when fund id is missing', async () => {
            const user = userEvent.setup()
            const onClose = vi.fn()

            // Create fund without id
            const fundWithoutId: Fund = {
                ...mockFund,
                id: undefined as any
            }

            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={fundWithoutId}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            const saveButton = screen.getByRole('button', { name: /保存/ })
            await user.click(saveButton)

            await waitFor(() => {
                expect(message.error).toHaveBeenCalledWith('基金信息不完整')
            })
        })
    })

    describe('Button States', () => {
        it('should have Cancel button enabled when not loading', () => {
            const onClose = vi.fn()
            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            const cancelButton = screen.getByRole('button', { name: /取消/ })
            expect(cancelButton).not.toBeDisabled()
        })
    })

    describe('Form Validation', () => {
        it('should require market field', async () => {
            const user = userEvent.setup()
            const onClose = vi.fn()

            const fundWithoutMarket: Fund = {
                ...mockFund,
                market: undefined as any
            }

            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={fundWithoutMarket}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            const saveButton = screen.getByRole('button', { name: /保存/ })
            await user.click(saveButton)

            // Should show validation error for required field
            await waitFor(() => {
                expect(screen.getByText('请选择交易市场')).toBeTruthy()
            })
        })

        it('should require category field', async () => {
            const user = userEvent.setup()
            const onClose = vi.fn()

            const fundWithoutCategory: Fund = {
                ...mockFund,
                category: undefined as any
            }

            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={fundWithoutCategory}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            const saveButton = screen.getByRole('button', { name: /保存/ })
            await user.click(saveButton)

            await waitFor(() => {
                expect(screen.getByText('请选择基金分类')).toBeTruthy()
            })
        })

        it('should require collectionFrequency field', async () => {
            const user = userEvent.setup()
            const onClose = vi.fn()

            const fundWithoutFrequency: Fund = {
                ...mockFund,
                collectionFrequency: undefined as any
            }

            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={fundWithoutFrequency}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            const saveButton = screen.getByRole('button', { name: /保存/ })
            await user.click(saveButton)

            await waitFor(() => {
                expect(screen.getByText('请选择采集频率')).toBeTruthy()
            })
        })

        it('should require dataSource field', async () => {
            const user = userEvent.setup()
            const onClose = vi.fn()

            const fundWithoutDataSource: Fund = {
                ...mockFund,
                dataSource: undefined as any
            }

            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={fundWithoutDataSource}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            const saveButton = screen.getByRole('button', { name: /保存/ })
            await user.click(saveButton)

            await waitFor(() => {
                expect(screen.getByText('请选择数据源')).toBeTruthy()
            })
        })
    })

    describe('Optional Field', () => {
        it('should allow description to be empty', async () => {
            const user = userEvent.setup()
            const onClose = vi.fn()
            const onSuccess = vi.fn()
            const { fundsApi } = await import('@/services/api/funds')

            const fundWithoutDescription: Fund = {
                ...mockFund,
                description: ''
            }

                // Mock successful API response
                ; (fundsApi.updateFund as ReturnType<typeof vi.fn>).mockResolvedValue({ success: true })

            render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={fundWithoutDescription}
                        onClose={onClose}
                        onSuccess={onSuccess}
                    />
                </TestWrapper>
            )

            const saveButton = screen.getByRole('button', { name: /保存/ })
            await user.click(saveButton)

            await waitFor(() => {
                expect(fundsApi.updateFund).toHaveBeenCalledWith(1, expect.objectContaining({
                    description: ''
                }))
            })
        })
    })

    describe('Form Reset', () => {
        it('should reset form when modal is closed and reopened', () => {
            const onClose = vi.fn()

            const { rerender } = render(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            // Close modal
            rerender(
                <TestWrapper>
                    <FundEditModal
                        visible={false}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            // Reopen modal
            rerender(
                <TestWrapper>
                    <FundEditModal
                        visible={true}
                        fund={mockFund}
                        onClose={onClose}
                    />
                </TestWrapper>
            )

            // Form should be rendered with fund data again
            expect(screen.getByText(/编辑基金/)).toBeTruthy()
            expect(screen.getByText('001593')).toBeTruthy()
        })
    })
})
