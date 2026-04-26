import SubscriptionCreate from '@/components/subscriptions/SubscriptionCreate'
import { configureStore } from '@reduxjs/toolkit'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { message } from 'antd'
import React from 'react'
import { Provider } from 'react-redux'
import { beforeEach, describe, expect, it, vi } from 'vitest'

// Mock functions
const { mockBatchCreateSubscriptions, mockDispatch } = vi.hoisted(() => ({
    mockBatchCreateSubscriptions: vi.fn(),
    mockDispatch: vi.fn()
}))

// Mock subscriptionsApi
vi.mock('@services/api/subscriptions', () => ({
    subscriptionsApi: {
        batchCreateSubscriptions: mockBatchCreateSubscriptions,
        getSubscriptions: vi.fn().mockResolvedValue({ success: true, data: [] })
    }
}))

// Mock stocksApi
vi.mock('@services/api/stocks', () => ({
    stocksApi: {
        searchTargets: vi.fn().mockResolvedValue({
            success: true,
            data: [
                { id: 1, code: '600000', name: '浦发银行', type: 'STOCK', market: 'SH' },
                { id: 2, code: '000001', name: '平安银行', type: 'STOCK', market: 'SZ' },
                { id: 3, code: '110001', name: '上证指数', type: 'FUND', market: 'SH' }
            ]
        })
    }
}))

// Mock useAppSelector
vi.mock('@store/hooks', () => ({
    useAppDispatch: () => mockDispatch,
    useAppSelector: (selector: any) => selector({
        subscriptions: {
            list: [],
            loading: false,
            error: null,
            filters: {}
        }
    })
}))

// Mock antd message
vi.mock('antd', async () => {
    const actual = await vi.importActual('antd')
    return {
        ...actual,
        message: {
            success: vi.fn(),
            error: vi.fn(),
            warning: vi.fn()
        }
    }
})

const createTestStore = () => {
    return configureStore({
        reducer: {
            subscriptions: (state = { list: [], loading: false, error: null, filters: {} }) => state
        }
    })
}

const TestWrapper: React.FC<{ children: React.ReactNode; store?: ReturnType<typeof createTestStore> }> = ({
    children,
    store = createTestStore()
}) => (
    <Provider store={store}>
        {children}
    </Provider>
)

describe('SubscriptionCreate', () => {
    const mockOnClose = vi.fn()
    const mockOnSuccess = vi.fn()

    beforeEach(() => {
        vi.clearAllMocks()
        mockBatchCreateSubscriptions.mockResolvedValue({
            code: 200,
            success: true,
            data: { successCount: 1, failCount: 0 }
        })
    })

    describe('Rendering', () => {
        it('should render modal with title', () => {
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            expect(screen.getByText('创建订阅')).toBeTruthy()
        })

        it('should render quick mode and custom mode cards', () => {
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            expect(screen.getByText('快速订阅')).toBeTruthy()
            expect(screen.getByText('自定义设置')).toBeTruthy()
        })

        it('should render symbol type selector', () => {
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            expect(screen.getByText('标的类型')).toBeTruthy()
        })

        it('should render symbol selector', () => {
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            expect(screen.getByText('选择标的')).toBeTruthy()
        })

        it('should render quick subscribe button in quick mode', () => {
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            expect(screen.getByRole('button', { name: /一键订阅/ })).toBeTruthy()
        })
    })

    describe('Quick Mode', () => {
        it('should show quick mode selected by default', () => {
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Quick mode card should have active class/style
            expect(screen.getByText('快速订阅')).toBeTruthy()
            expect(screen.getByText('一键订阅涨跌幅超过1%的风险提醒')).toBeTruthy()
        })

        it('should send target_change_percent=1 when quick subscribing', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Focus on the symbol select to load search results
            const selectTrigger = screen.getByPlaceholderText('搜索股票代码或名称...')
            await user.click(selectTrigger)

            // Wait for search results to load
            await waitFor(() => {
                expect(screen.queryByText('浦发银行')).toBeTruthy()
            })

            // Select a symbol from the dropdown
            await user.click(screen.getByText('浦发银行'))

            // Click quick subscribe button
            const quickSubscribeButton = screen.getByRole('button', { name: /一键订阅/ })
            await user.click(quickSubscribeButton)

            // Verify the API was called with target_change_percent=1
            await waitFor(() => {
                expect(mockBatchCreateSubscriptions).toHaveBeenCalledWith(
                    expect.objectContaining({
                        alertType: 'PERCENTAGE_CHANGE',
                        targetChangePercent: 1.0
                    })
                )
            })
        })

        it('should show correct alert info in quick mode', () => {
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Should show quick mode description
            expect(screen.getByText(/系统将自动为您设置涨跌幅1%的提醒条件/)).toBeTruthy()
        })

        it('should call onSuccess and onClose after quick subscribe success', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Focus on the symbol select to load search results
            const selectTrigger = screen.getByPlaceholderText('搜索股票代码或名称...')
            await user.click(selectTrigger)

            // Wait for search results
            await waitFor(() => {
                expect(screen.queryByText('浦发银行')).toBeTruthy()
            })

            // Select a symbol
            await user.click(screen.getByText('浦发银行'))

            // Click quick subscribe
            const quickSubscribeButton = screen.getByRole('button', { name: /一键订阅/ })
            await user.click(quickSubscribeButton)

            // Should call onSuccess and onClose
            await waitFor(() => {
                expect(mockOnSuccess).toHaveBeenCalled()
                expect(mockOnClose).toHaveBeenCalled()
            })
        })
    })

    describe('Custom Mode', () => {
        it('should switch to custom mode when custom card is clicked', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Click custom mode card
            await user.click(screen.getByText('自定义设置'))

            // Should show custom mode description
            await waitFor(() => {
                expect(screen.getByText(/您可以自定义提醒条件/)).toBeTruthy()
            })
        })

        it('should show alert type selector in custom mode', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Click custom mode card
            await user.click(screen.getByText('自定义设置'))

            // Should show alert type selector
            await waitFor(() => {
                expect(screen.getByText('提醒类型')).toBeTruthy()
            })
        })

        it('should send custom parameters when creating custom subscription', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Click custom mode card
            await user.click(screen.getByText('自定义设置'))

            await waitFor(() => {
                expect(screen.getByText('提醒类型')).toBeTruthy()
            })

            // Focus on the symbol select to load search results
            const selectTrigger = screen.getByPlaceholderText('搜索股票代码或名称...')
            await user.click(selectTrigger)

            // Wait for search results
            await waitFor(() => {
                expect(screen.queryByText('浦发银行')).toBeTruthy()
            })

            // Select a symbol
            await user.click(screen.getByText('浦发银行'))

            // Select alert type
            const alertTypeSelect = screen.getByText('提醒类型').parentElement?.querySelector('.ant-select') || screen.getByRole('combobox', { name: /提醒类型/ })
            await user.click(alertTypeSelect as Element)
            await waitFor(() => {
                expect(screen.getByText('价格上限')).toBeTruthy()
            })
            await user.click(screen.getByText('价格上限'))

            // Click create subscription button
            const createButton = screen.getByRole('button', { name: /创建订阅/ })
            await user.click(createButton)

            // Verify the API was called with custom parameters
            await waitFor(() => {
                expect(mockBatchCreateSubscriptions).toHaveBeenCalledWith(
                    expect.objectContaining({
                        alertType: 'PRICE_ABOVE'
                    })
                )
            })
        })

        it('should show threshold input for percentage change in custom mode', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Click custom mode card
            await user.click(screen.getByText('自定义设置'))

            await waitFor(() => {
                expect(screen.getByText('提醒类型')).toBeTruthy()
            })

            // Select percentage change alert type
            const alertTypeSelect = screen.getByText('提醒类型').parentElement?.querySelector('.ant-select') || screen.getByRole('combobox', { name: /提醒类型/ })
            await user.click(alertTypeSelect as Element)
            await waitFor(() => {
                expect(screen.getByText('涨跌幅')).toBeTruthy()
            })
            await user.click(screen.getByText('涨跌幅'))

            // Should show targetChangePercent input
            await waitFor(() => {
                expect(screen.getByText('涨跌幅阈值')).toBeTruthy()
            })
        })

        it('should show target price input for price alerts in custom mode', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Click custom mode card
            await user.click(screen.getByText('自定义设置'))

            await waitFor(() => {
                expect(screen.getByText('提醒类型')).toBeTruthy()
            })

            // Select price above alert type
            const alertTypeSelect = screen.getByText('提醒类型').parentElement?.querySelector('.ant-select') || screen.getByRole('combobox', { name: /提醒类型/ })
            await user.click(alertTypeSelect as Element)
            await waitFor(() => {
                expect(screen.getByText('价格上限')).toBeTruthy()
            })
            await user.click(screen.getByText('价格上限'))

            // Should show target price input
            await waitFor(() => {
                expect(screen.getByText('目标价格上限')).toBeTruthy()
            })
        })

        it('should show remark input in custom mode', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Click custom mode card
            await user.click(screen.getByText('自定义设置'))

            // Should show remark label
            await waitFor(() => {
                expect(screen.getByText('备注')).toBeTruthy()
            })
        })
    })

    describe('Mode Switching UI', () => {
        it('should switch from quick to custom mode', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Initially in quick mode - should see quick mode button
            expect(screen.getByRole('button', { name: /一键订阅/ })).toBeTruthy()

            // Click custom mode
            await user.click(screen.getByText('自定义设置'))

            // Should now see custom mode button
            await waitFor(() => {
                expect(screen.getByRole('button', { name: /创建订阅/ })).toBeTruthy()
            })

            // Quick mode button should be gone
            expect(screen.queryByRole('button', { name: /一键订阅/ })).toBeNull()
        })

        it('should switch from custom to quick mode', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Click custom mode first
            await user.click(screen.getByText('自定义设置'))

            await waitFor(() => {
                expect(screen.getByRole('button', { name: /创建订阅/ })).toBeTruthy()
            })

            // Click quick mode
            await user.click(screen.getByText('快速订阅'))

            // Should now see quick mode button
            await waitFor(() => {
                expect(screen.getByRole('button', { name: /一键订阅/ })).toBeTruthy()
            })

            // Custom mode button should be gone
            expect(screen.queryByRole('button', { name: /创建订阅/ })).toBeNull()
        })

        it('should preserve form data when switching modes', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Focus on the symbol select to load search results
            const selectTrigger = screen.getByPlaceholderText('搜索股票代码或名称...')
            await user.click(selectTrigger)

            // Wait for search results
            await waitFor(() => {
                expect(screen.queryByText('浦发银行')).toBeTruthy()
            })

            // Select a symbol in quick mode
            await user.click(screen.getByText('浦发银行'))

            // Switch to custom mode
            await user.click(screen.getByText('自定义设置'))

            await waitFor(() => {
                expect(screen.getByRole('button', { name: /创建订阅/ })).toBeTruthy()
            })

            // The selected symbol should still be visible (in "已选标的" section)
            // This tests that the form state is preserved
            await waitFor(() => {
                expect(screen.getByText(/浦发银行/)).toBeTruthy()
            })
        })
    })

    describe('Error Handling', () => {
        it('should show warning when no symbol selected', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Click quick subscribe without selecting any symbol
            const quickSubscribeButton = screen.getByRole('button', { name: /一键订阅/ })
            await user.click(quickSubscribeButton)

            await waitFor(() => {
                expect(message.warning).toHaveBeenCalledWith('请选择至少一个标的')
            })
        })

        it('should show error when API fails', async () => {
            const user = userEvent.setup()
            mockBatchCreateSubscriptions.mockRejectedValue(new Error('订阅失败'))

            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Focus on the symbol select to load search results
            const selectTrigger = screen.getByPlaceholderText('搜索股票代码或名称...')
            await user.click(selectTrigger)

            // Wait for search results
            await waitFor(() => {
                expect(screen.queryByText('浦发银行')).toBeTruthy()
            })

            // Select a symbol
            await user.click(screen.getByText('浦发银行'))

            // Click quick subscribe
            const quickSubscribeButton = screen.getByRole('button', { name: /一键订阅/ })
            await user.click(quickSubscribeButton)

            await waitFor(() => {
                expect(message.error).toHaveBeenCalledWith('订阅失败')
            })
        })

        it('should not call onSuccess when API fails', async () => {
            const user = userEvent.setup()
            mockBatchCreateSubscriptions.mockRejectedValue(new Error('订阅失败'))

            render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Focus on the symbol select to load search results
            const selectTrigger = screen.getByPlaceholderText('搜索股票代码或名称...')
            await user.click(selectTrigger)

            // Wait for search results
            await waitFor(() => {
                expect(screen.queryByText('浦发银行')).toBeTruthy()
            })

            // Select a symbol
            await user.click(screen.getByText('浦发银行'))

            // Click quick subscribe
            const quickSubscribeButton = screen.getByRole('button', { name: /一键订阅/ })
            await user.click(quickSubscribeButton)

            await waitFor(() => {
                expect(mockOnSuccess).not.toHaveBeenCalled()
            })
        })
    })

    describe('Form Reset', () => {
        it('should reset form when modal is closed and reopened', async () => {
            const user = userEvent.setup()
            const { rerender } = render(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Focus on the symbol select to load search results
            const selectTrigger = screen.getByPlaceholderText('搜索股票代码或名称...')
            await user.click(selectTrigger)

            // Wait for search results
            await waitFor(() => {
                expect(screen.queryByText('浦发银行')).toBeTruthy()
            })

            // Select a symbol
            await user.click(screen.getByText('浦发银行'))

            // Close modal
            await rerender(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={false}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Reopen modal
            await rerender(
                <TestWrapper>
                    <SubscriptionCreate
                        visible={true}
                        onClose={mockOnClose}
                        onSuccess={mockOnSuccess}
                    />
                </TestWrapper>
            )

            // Form should be reset - no selected symbols shown
            await waitFor(() => {
                expect(screen.getByText('暂未选择任何标的')).toBeTruthy()
            })
        })
    })
})
