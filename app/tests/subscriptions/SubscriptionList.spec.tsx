import SubscriptionList from '@/pages/subscriptions/SubscriptionList'
import { Subscription } from '@/services/api/subscriptions'
import { configureStore } from '@reduxjs/toolkit'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { message } from 'antd'
import React from 'react'
import { Provider } from 'react-redux'
import { beforeEach, describe, expect, it, vi } from 'vitest'

// Mock data
const mockSubscriptions: Subscription[] = [
    {
        id: 1,
        userId: 1,
        symbol: '600000',
        symbolName: '浦发银行',
        symbolType: 'STOCK',
        alertType: 'PRICE_ABOVE',
        targetPrice: 15.5,
        status: 'ACTIVE',
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-01T10:00:00Z'
    },
    {
        id: 2,
        userId: 1,
        symbol: '000001',
        symbolName: '平安银行',
        symbolType: 'STOCK',
        alertType: 'PERCENTAGE_CHANGE',
        targetChangePercent: 2.0,
        status: 'INACTIVE',
        createdAt: '2024-01-01T11:00:00Z',
        updatedAt: '2024-01-01T11:00:00Z'
    },
    {
        id: 3,
        userId: 1,
        symbol: '110001',
        symbolName: '上证指数',
        symbolType: 'FUND',
        alertType: 'PRICE_BELOW',
        targetPrice: 3000,
        status: 'ACTIVE',
        createdAt: '2024-01-01T12:00:00Z',
        updatedAt: '2024-01-01T12:00:00Z'
    }
]

// Mock functions
const { mockFetchSubscriptions, mockBatchDeleteSubscriptions, mockBatchActivateSubscriptions, mockBatchDeactivateSubscriptions, mockDispatch } = vi.hoisted(() => ({
    mockFetchSubscriptions: vi.fn(),
    mockBatchDeleteSubscriptions: vi.fn().mockResolvedValue({ success: true }),
    mockBatchActivateSubscriptions: vi.fn().mockResolvedValue({ success: true }),
    mockBatchDeactivateSubscriptions: vi.fn().mockResolvedValue({ success: true }),
    mockDispatch: vi.fn()
}))

// Mock subscriptionsSlice
vi.mock('@store/slices/subscriptionsSlice', () => ({
    fetchSubscriptions: mockFetchSubscriptions,
    batchDeleteSubscriptions: mockBatchDeleteSubscriptions,
    batchActivateSubscriptions: mockBatchActivateSubscriptions,
    batchDeactivateSubscriptions: mockBatchDeactivateSubscriptions,
    clearFilters: vi.fn(),
    setFilters: vi.fn(),
    default: () => ({
        list: [],
        loading: false,
        error: null,
        filters: {}
    })
}))

// Mock useAppSelector
vi.mock('@store/hooks', () => ({
    useAppDispatch: () => mockDispatch,
    useAppSelector: (selector: any) => selector({
        subscriptions: {
            list: mockSubscriptions,
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
            subscriptions: (state = { list: mockSubscriptions, loading: false, error: null, filters: {} }) => state
        }
    })
}

const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
    <Provider store={createTestStore()}>
        {children}
    </Provider>
)

describe('SubscriptionList', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('Rendering', () => {
        it('should render subscription list page with title', () => {
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            expect(screen.getByText('订阅管理')).toBeTruthy()
        })

        it('should render Create Subscription button', () => {
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            expect(screen.getByRole('button', { name: /创建订阅/ })).toBeTruthy()
        })

        it('should render table with subscription data', () => {
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Check symbols are displayed
            expect(screen.getByText('600000')).toBeTruthy()
            expect(screen.getByText('000001')).toBeTruthy()
            expect(screen.getByText('110001')).toBeTruthy()
        })

        it('should render Edit button for each row', () => {
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            const editButtons = screen.getAllByRole('button', { name: /编辑/ })
            expect(editButtons.length).toBe(3)
        })
    })

    describe('Batch Selection', () => {
        it('should show checkboxes for row selection', () => {
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Find checkboxes in the table
            const checkboxes = screen.getAllByRole('checkbox')
            // Table has header checkbox + 3 row checkboxes
            expect(checkboxes.length).toBe(4)
        })

        it('should show batch action area when rows are selected', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes (not the header one)
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)

            // Click to select first row
            await user.click(rowCheckboxes[0])

            // Batch action area should appear
            await waitFor(() => {
                expect(screen.getByText(/已选择 1 项/)).toBeTruthy()
            })
        })

        it('should show correct selection count when multiple rows selected', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)

            // Select first two rows
            await user.click(rowCheckboxes[0])
            await user.click(rowCheckboxes[1])

            // Batch action area should show correct count
            await waitFor(() => {
                expect(screen.getByText(/已选择 2 项/)).toBeTruthy()
            })
        })

        it('should display batch action buttons when rows selected', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)

            // Select first row
            await user.click(rowCheckboxes[0])

            // Batch action buttons should appear
            await waitFor(() => {
                expect(screen.getByRole('button', { name: /批量启用/ })).toBeTruthy()
                expect(screen.getByRole('button', { name: /批量停用/ })).toBeTruthy()
                expect(screen.getByRole('button', { name: /批量删除/ })).toBeTruthy()
            })
        })
    })

    describe('Batch Delete', () => {
        it('should call batchDeleteSubscriptions API when batch delete is confirmed', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)

            // Select first two rows
            await user.click(rowCheckboxes[0])
            await user.click(rowCheckboxes[1])

            // Wait for batch action area
            await waitFor(() => {
                expect(screen.getByText(/已选择 2 项/)).toBeTruthy()
            })

            // Click batch delete button
            const deleteButton = screen.getByRole('button', { name: /批量删除/ })
            await user.click(deleteButton)

            // Should show confirmation Popconfirm
            await waitFor(() => {
                expect(screen.getByText(/确定要删除选中的 2 个订阅吗/)).toBeTruthy()
            })

            // Click confirm
            const confirmButton = screen.getByRole('button', { name: /确认删除/ })
            await user.click(confirmButton)

            // Should call batchDeleteSubscriptions with the selected IDs
            await waitFor(() => {
                expect(mockBatchDeleteSubscriptions).toHaveBeenCalledWith([1, 2])
            })
        })

        it('should show success message after successful batch delete', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes and select one
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)
            await user.click(rowCheckboxes[0])

            await waitFor(() => {
                expect(screen.getByText(/已选择 1 项/)).toBeTruthy()
            })

            // Click batch delete
            const deleteButton = screen.getByRole('button', { name: /批量删除/ })
            await user.click(deleteButton)

            await waitFor(() => {
                const confirmButton = screen.getByRole('button', { name: /确认删除/ })
                user.click(confirmButton)
            })

            await waitFor(() => {
                expect(message.success).toHaveBeenCalledWith('成功删除 1 个订阅')
            })
        })

        it('should clear selection after batch delete', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes and select one
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)
            await user.click(rowCheckboxes[0])

            await waitFor(() => {
                expect(screen.getByText(/已选择 1 项/)).toBeTruthy()
            })

            // Click batch delete and confirm
            const deleteButton = screen.getByRole('button', { name: /批量删除/ })
            await user.click(deleteButton)

            await waitFor(() => {
                const confirmButton = screen.getByRole('button', { name: /确认删除/ })
                user.click(confirmButton)
            })

            // Selection should be cleared (batch action area should disappear)
            await waitFor(() => {
                expect(screen.queryByText(/已选择/)).toBeNull()
            })
        })

        it('should show error message when batch delete fails', async () => {
            const user = userEvent.setup()
            mockBatchDeleteSubscriptions.mockRejectedValue(new Error('批量删除失败'))

            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes and select one
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)
            await user.click(rowCheckboxes[0])

            await waitFor(() => {
                expect(screen.getByText(/已选择 1 项/)).toBeTruthy()
            })

            // Click batch delete and confirm
            const deleteButton = screen.getByRole('button', { name: /批量删除/ })
            await user.click(deleteButton)

            await waitFor(() => {
                const confirmButton = screen.getByRole('button', { name: /确认删除/ })
                user.click(confirmButton)
            })

            await waitFor(() => {
                expect(message.error).toHaveBeenCalledWith('批量删除失败')
            })
        })
    })

    describe('Batch Activate/Deactivate', () => {
        it('should call batchActivateSubscriptions when batch activate is clicked', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes and select one
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)
            await user.click(rowCheckboxes[0])

            await waitFor(() => {
                expect(screen.getByText(/已选择 1 项/)).toBeTruthy()
            })

            // Click batch activate
            const activateButton = screen.getByRole('button', { name: /批量启用/ })
            await user.click(activateButton)

            await waitFor(() => {
                expect(mockBatchActivateSubscriptions).toHaveBeenCalledWith([1])
            })
        })

        it('should call batchDeactivateSubscriptions when batch deactivate is clicked', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes and select one
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)
            await user.click(rowCheckboxes[0])

            await waitFor(() => {
                expect(screen.getByText(/已选择 1 项/)).toBeTruthy()
            })

            // Click batch deactivate
            const deactivateButton = screen.getByRole('button', { name: /批量停用/ })
            await user.click(deactivateButton)

            await waitFor(() => {
                expect(mockBatchDeactivateSubscriptions).toHaveBeenCalledWith([1])
            })
        })

        it('should show success message after batch activate', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes and select one
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)
            await user.click(rowCheckboxes[0])

            await waitFor(() => {
                expect(screen.getByText(/已选择 1 项/)).toBeTruthy()
            })

            // Click batch activate
            const activateButton = screen.getByRole('button', { name: /批量启用/ })
            await user.click(activateButton)

            await waitFor(() => {
                expect(message.success).toHaveBeenCalledWith('成功启用 1 个订阅')
            })
        })

        it('should show success message after batch deactivate', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes and select one
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)
            await user.click(rowCheckboxes[0])

            await waitFor(() => {
                expect(screen.getByText(/已选择 1 项/)).toBeTruthy()
            })

            // Click batch deactivate
            const deactivateButton = screen.getByRole('button', { name: /批量停用/ })
            await user.click(deactivateButton)

            await waitFor(() => {
                expect(message.success).toHaveBeenCalledWith('成功停用 1 个订阅')
            })
        })

        it('should show error message when batch activate fails', async () => {
            const user = userEvent.setup()
            mockBatchActivateSubscriptions.mockRejectedValue(new Error('批量启用失败'))

            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes and select one
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)
            await user.click(rowCheckboxes[0])

            await waitFor(() => {
                expect(screen.getByText(/已选择 1 项/)).toBeTruthy()
            })

            // Click batch activate
            const activateButton = screen.getByRole('button', { name: /批量启用/ })
            await user.click(activateButton)

            await waitFor(() => {
                expect(message.error).toHaveBeenCalledWith('批量启用失败')
            })
        })

        it('should show error message when batch deactivate fails', async () => {
            const user = userEvent.setup()
            mockBatchDeactivateSubscriptions.mockRejectedValue(new Error('批量停用失败'))

            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes and select one
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)
            await user.click(rowCheckboxes[0])

            await waitFor(() => {
                expect(screen.getByText(/已选择 1 项/)).toBeTruthy()
            })

            // Click batch deactivate
            const deactivateButton = screen.getByRole('button', { name: /批量停用/ })
            await user.click(deactivateButton)

            await waitFor(() => {
                expect(message.error).toHaveBeenCalledWith('批量停用失败')
            })
        })

        it('should clear selection after batch operation', async () => {
            const user = userEvent.setup()
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            // Get all row checkboxes and select one
            const rowCheckboxes = screen.getAllByRole('checkbox').slice(1)
            await user.click(rowCheckboxes[0])

            await waitFor(() => {
                expect(screen.getByText(/已选择 1 项/)).toBeTruthy()
            })

            // Click batch activate
            const activateButton = screen.getByRole('button', { name: /批量启用/ })
            await user.click(activateButton)

            // Selection should be cleared
            await waitFor(() => {
                expect(screen.queryByText(/已选择/)).toBeNull()
            })
        })
    })

    describe('Data Refresh', () => {
        it('should dispatch fetchSubscriptions on mount', () => {
            render(
                <TestWrapper>
                    <SubscriptionList />
                </TestWrapper>
            )

            expect(mockFetchSubscriptions).toHaveBeenCalled()
        })
    })
})
