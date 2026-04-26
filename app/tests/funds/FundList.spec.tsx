import FundList from '@/pages/funds/FundList'
import { Fund } from '@/types'
import { configureStore } from '@reduxjs/toolkit'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { message, Modal } from 'antd'
import React from 'react'
import { Provider } from 'react-redux'
import { beforeEach, describe, expect, it, vi } from 'vitest'

// Mock data
const mockFunds: Fund[] = [
    {
        id: 1,
        code: '001593',
        name: '天弘创业板ETF联接C',
        type: '指数基金',
        market: 'SZ',
        category: 'INDEX',
        description: '测试描述1',
        collectionFrequency: 1440,
        dataSource: 'SINA',
        active: true
    },
    {
        id: 2,
        code: '000001',
        name: '华夏成长混合',
        type: '混合型',
        market: 'SH',
        category: 'MIXED',
        description: '测试描述2',
        collectionFrequency: 60,
        dataSource: 'EASTMONEY',
        active: false
    }
]

// Use vi.hoisted to define mocks that reference each other
const { mockFetchAllFunds, mockDeleteFund, mockActivateFund, mockDeactivateFund, mockDispatch } = vi.hoisted(() => ({
    mockFetchAllFunds: vi.fn(),
    mockDeleteFund: vi.fn().mockResolvedValue({ success: true }),
    mockActivateFund: vi.fn().mockResolvedValue({ success: true }),
    mockDeactivateFund: vi.fn().mockResolvedValue({ success: true }),
    mockDispatch: vi.fn()
}))

// Mock fundsSlice
vi.mock('@store/slices/fundsSlice', () => ({
    fetchAllFunds: mockFetchAllFunds,
    deleteFund: mockDeleteFund,
    activateFund: mockActivateFund,
    deactivateFund: mockDeactivateFund,
    default: () => ({
        list: [],
        selectedFund: null,
        loading: false,
        error: null
    })
}))

// Mock useAppSelector
vi.mock('@store/hooks', () => ({
    useAppDispatch: () => mockDispatch,
    useAppSelector: (selector: any) => selector({
        funds: {
            list: mockFunds,
            loading: false,
            error: null
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

// Mock Modal.confirm
const mockConfirm = vi.fn()
vi.spyOn(Modal, 'confirm').mockImplementation(mockConfirm)

const createTestStore = () => {
    return configureStore({
        reducer: {
            funds: (state = { list: mockFunds, loading: false, error: null }) => state
        }
    })
}

const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
    <Provider store={createTestStore()}>
        {children}
    </Provider>
)

describe('FundList', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        mockConfirm.mockImplementation((config) => {
            // Simulate clicking OK immediately for tests
            if (config.onOk) {
                // Call onOk asynchronously to allow React to update
                setTimeout(() => config.onOk(), 0)
            }
            return {} as any
        })
    })

    describe('Rendering', () => {
        it('should render fund list page with title', () => {
            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            expect(screen.getByText('基金管理')).toBeTruthy()
        })

        it('should render Add Fund button', () => {
            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            expect(screen.getByRole('button', { name: /添加基金/ })).toBeTruthy()
        })

        it('should render table with fund data', () => {
            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Check fund codes are displayed
            expect(screen.getByText('001593')).toBeTruthy()
            expect(screen.getByText('000001')).toBeTruthy()

            // Check fund names are displayed
            expect(screen.getByText('天弘创业板ETF联接C')).toBeTruthy()
            expect(screen.getByText('华夏成长混合')).toBeTruthy()
        })

        it('should render Edit and Delete buttons for each row', () => {
            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Each row should have Edit and Delete buttons
            const editButtons = screen.getAllByRole('button', { name: /编辑/ })
            const deleteButtons = screen.getAllByRole('button', { name: /删除/ })

            expect(editButtons.length).toBe(2)
            expect(deleteButtons.length).toBe(2)
        })

        it('should render Switch components for active state', () => {
            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            const switches = screen.getAllByRole('switch')
            expect(switches.length).toBe(2)
        })
    })

    describe('Delete Functionality', () => {
        it('should show confirmation modal when delete button is clicked', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Click the first delete button
            const deleteButtons = screen.getAllByRole('button', { name: /删除/ })
            await user.click(deleteButtons[0])

            // Modal.confirm should be called
            expect(Modal.confirm).toHaveBeenCalled()
        })

        it('should call deleteFund API after confirmation', async () => {
            const user = userEvent.setup()
            mockDeleteFund.mockResolvedValue({ success: true })

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Click delete button
            const deleteButtons = screen.getAllByRole('button', { name: /删除/ })
            await user.click(deleteButtons[0])

            // Wait for the async onOk callback
            await waitFor(() => {
                expect(mockDeleteFund).toHaveBeenCalledWith(1)
            })
        })

        it('should show success message after successful delete', async () => {
            const user = userEvent.setup()
            mockDeleteFund.mockResolvedValue({ success: true })

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Click delete button
            const deleteButtons = screen.getAllByRole('button', { name: /删除/ })
            await user.click(deleteButtons[0])

            await waitFor(() => {
                expect(message.success).toHaveBeenCalledWith('基金 "天弘创业板ETF联接C" 已删除')
            })
        })

        it('should show error message when delete fails', async () => {
            const user = userEvent.setup()
            mockDeleteFund.mockRejectedValue(new Error('删除失败：基金不存在'))

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Click delete button
            const deleteButtons = screen.getAllByRole('button', { name: /删除/ })
            await user.click(deleteButtons[0])

            await waitFor(() => {
                expect(message.error).toHaveBeenCalledWith('删除失败：基金不存在')
            })
        })

        it('should remove fund from list after successful delete', async () => {
            const user = userEvent.setup()
            mockDeleteFund.mockResolvedValue({ success: true })

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Click delete button for first fund
            const deleteButtons = screen.getAllByRole('button', { name: /删除/ })
            await user.click(deleteButtons[0])

            await waitFor(() => {
                // After delete, the first fund should no longer be visible
                expect(screen.queryByText('天弘创业板ETF联接C')).toBeNull()
            })
        })

        it('should disable delete button while deleting', async () => {
            const user = userEvent.setup()
            mockDeleteFund.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({ success: true }), 100)))

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Click delete button
            const deleteButtons = screen.getAllByRole('button', { name: /删除/ })
            await user.click(deleteButtons[0])

            // The delete button should be disabled while deleting
            await waitFor(() => {
                expect(deleteButtons[0]).toBeDisabled()
            })
        })
    })

    describe('Activate/Deactivate Toggle', () => {
        it('should call activateFund when toggling inactive fund', async () => {
            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Find the switch for the second fund (inactive)
            const switches = screen.getAllByRole('switch')
            await switches[1].click()

            await waitFor(() => {
                expect(mockActivateFund).toHaveBeenCalledWith(2)
            })
        })

        it('should call deactivateFund when toggling active fund', async () => {
            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Find the switch for the first fund (active)
            const switches = screen.getAllByRole('switch')
            await switches[0].click()

            await waitFor(() => {
                expect(mockDeactivateFund).toHaveBeenCalledWith(1)
            })
        })

        it('should show success message when activating fund', async () => {
            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Find the switch for the second fund (inactive)
            const switches = screen.getAllByRole('switch')
            await switches[1].click()

            await waitFor(() => {
                expect(message.success).toHaveBeenCalledWith('基金 "华夏成长混合" 已激活')
            })
        })

        it('should show success message when deactivating fund', async () => {
            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Find the switch for the first fund (active)
            const switches = screen.getAllByRole('switch')
            await switches[0].click()

            await waitFor(() => {
                expect(message.success).toHaveBeenCalledWith('基金 "天弘创业板ETF联接C" 已停用')
            })
        })

        it('should show error message when toggle fails', async () => {
            mockActivateFund.mockRejectedValue(new Error('激活失败'))

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Find the switch for the second fund (inactive)
            const switches = screen.getAllByRole('switch')
            await switches[1].click()

            await waitFor(() => {
                expect(message.error).toHaveBeenCalledWith('激活失败')
            })
        })
    })

    describe('Loading States', () => {
        it('should show loading state on delete button during delete operation', async () => {
            const user = userEvent.setup()
            mockDeleteFund.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({ success: true }), 100)))

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Click delete button
            const deleteButtons = screen.getAllByRole('button', { name: /删除/ })
            await user.click(deleteButtons[0])

            // Check if button has loading state (antd uses aria-busy or has loading class)
            await waitFor(() => {
                expect(deleteButtons[0]).toHaveAttribute('aria-busy', 'true')
            })
        })

        it('should show loading state on switch during toggle operation', async () => {
            mockActivateFund.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({ success: true }), 100)))

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Click the switch for inactive fund
            const switches = screen.getAllByRole('switch')
            await switches[1].click()

            // Check if switch has loading state
            await waitFor(() => {
                expect(switches[1]).toHaveClass('ant-switch-loading')
            })
        })

        it('should restore normal state after delete operation completes', async () => {
            const user = userEvent.setup()
            mockDeleteFund.mockResolvedValue({ success: true })

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Click delete button
            const deleteButtons = screen.getAllByRole('button', { name: /删除/ })
            await user.click(deleteButtons[0])

            // Wait for delete to complete
            await waitFor(() => {
                expect(deleteButtons[0]).not.toBeDisabled()
            })
        })

        it('should restore normal state after toggle operation completes', async () => {
            mockActivateFund.mockResolvedValue({ success: true })

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Click the switch for inactive fund
            const switches = screen.getAllByRole('switch')
            await switches[1].click()

            // Wait for toggle to complete
            await waitFor(() => {
                expect(switches[1]).not.toHaveClass('ant-switch-loading')
            })
        })
    })

    describe('Button States', () => {
        it('should disable edit button while its row is deleting', async () => {
            const user = userEvent.setup()
            mockDeleteFund.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({ success: true }), 100)))

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Click delete button for first row
            const deleteButtons = screen.getAllByRole('button', { name: /删除/ })
            await user.click(deleteButtons[0])

            // Edit button in the same row should be disabled
            const editButtons = screen.getAllByRole('button', { name: /编辑/ })
            await waitFor(() => {
                expect(editButtons[0]).toBeDisabled()
            })
        })

        it('should disable both edit and delete buttons for a row during its delete operation', async () => {
            const user = userEvent.setup()
            mockDeleteFund.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({ success: true }), 100)))

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            // Click delete button for second row
            const deleteButtons = screen.getAllByRole('button', { name: /删除/ })
            await user.click(deleteButtons[1])

            // Both buttons in the second row should be disabled
            const editButtons = screen.getAllByRole('button', { name: /编辑/ })
            await waitFor(() => {
                expect(editButtons[1]).toBeDisabled()
                expect(deleteButtons[1]).toBeDisabled()
            })
        })
    })

    describe('API Error Handling', () => {
        it('should handle network error during delete', async () => {
            const user = userEvent.setup()
            mockDeleteFund.mockRejectedValue(new Error('网络连接失败'))

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            const deleteButtons = screen.getAllByRole('button', { name: /删除/ })
            await user.click(deleteButtons[0])

            await waitFor(() => {
                expect(message.error).toHaveBeenCalledWith('网络连接失败')
            })
        })

        it('should handle network error during activate', async () => {
            mockActivateFund.mockRejectedValue(new Error('网络连接失败'))

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            const switches = screen.getAllByRole('switch')
            await switches[1].click()

            await waitFor(() => {
                expect(message.error).toHaveBeenCalledWith('网络连接失败')
            })
        })

        it('should handle network error during deactivate', async () => {
            mockDeactivateFund.mockRejectedValue(new Error('网络连接失败'))

            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            const switches = screen.getAllByRole('switch')
            await switches[0].click()

            await waitFor(() => {
                expect(message.error).toHaveBeenCalledWith('网络连接失败')
            })
        })
    })

    describe('Data Refresh', () => {
        it('should dispatch fetchAllFunds on mount', () => {
            render(
                <TestWrapper>
                    <FundList />
                </TestWrapper>
            )

            expect(mockFetchAllFunds).toHaveBeenCalled()
        })
    })
})
