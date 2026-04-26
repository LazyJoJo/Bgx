import { fundsApi } from '@/services/api/funds'
import fundsReducer, {
    activateFund,
    clearError,
    deactivateFund,
    deleteFund,
    fetchAllFunds,
    fetchFundById,
    fetchFundDetail,
    fetchFunds,
    setSelectedFund,
    updateFund
} from '@/store/slices/fundsSlice'
import { Fund, FundUpdateRequest } from '@/types'
import { configureStore } from '@reduxjs/toolkit'
import { beforeEach, describe, expect, it } from 'vitest'

// Mock data - collectionFrequency uses numeric values
const mockFund: Fund = {
    id: 1,
    code: '000001',
    name: '华夏成长混合',
    type: '混合型',
    market: 'SH',
    category: 'MIXED',
    description: 'Test fund description',
    collectionFrequency: 60,
    dataSource: 'EASTMONEY',
    active: true,
    manager: 'Test Manager',
    establishmentDate: '2020-01-01',
    fundSize: 1000000000,
    nav: 1.5,
    dayGrowth: 0.5,
    weekGrowth: 1.2,
    monthGrowth: 3.5,
    yearGrowth: 10.5,
    currentNav: 1.55,
    dailyChange: 0.05,
    dailyChangePercent: 3.33,
    annualizedReturn: 12.5,
    riskLevel: 'MEDIUM'
}

const mockFund2: Fund = {
    id: 2,
    code: '001593',
    name: '天弘创业板ETF联接C',
    type: '指数基金',
    market: 'SZ',
    category: 'INDEX',
    description: 'Test description 2',
    collectionFrequency: 1440,
    dataSource: 'SINA',
    active: false
}

const mockFunds: Fund[] = [mockFund, mockFund2]

// Mock fundsApi
vi.mock('@/services/api/funds', () => ({
    fundsApi: {
        getFundTargets: vi.fn(),
        getFundDetail: vi.fn(),
        getAllFunds: vi.fn(),
        getFundById: vi.fn(),
        updateFund: vi.fn(),
        deleteFund: vi.fn(),
        activateFund: vi.fn(),
        deactivateFund: vi.fn()
    }
}))

describe('fundsSlice', () => {
    let store: ReturnType<typeof configureStore>

    beforeEach(() => {
        store = configureStore({
            reducer: {
                funds: fundsReducer
            }
        })
        vi.clearAllMocks()
    })

    describe('Initial State', () => {
        it('should have correct initial state', () => {
            const state = store.getState().funds
            expect(state.list).toEqual([])
            expect(state.selectedFund).toBeNull()
            expect(state.loading).toBe(false)
            expect(state.error).toBeNull()
        })
    })

    describe('fetchFunds thunk', () => {
        it('should set loading=true when pending', () => {
            vi.mocked(fundsApi.getFundTargets).mockImplementation(() => new Promise(() => { }))
            store.dispatch(fetchFunds())
            expect(store.getState().funds.loading).toBe(true)
            expect(store.getState().funds.error).toBeNull()
        })

        it('should set list and loading=false when fulfilled', async () => {
            vi.mocked(fundsApi.getFundTargets).mockResolvedValue({
                success: true,
                data: mockFunds,
                error: null
            } as any)
            await store.dispatch(fetchFunds())
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.list).toEqual(mockFunds)
        })

        it('should set error and loading=false when rejected', async () => {
            vi.mocked(fundsApi.getFundTargets).mockRejectedValue(new Error('Failed to fetch funds'))
            await store.dispatch(fetchFunds())
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.error).toBeTruthy()
        })
    })

    describe('fetchFundDetail thunk', () => {
        it('should set loading=true when pending', () => {
            vi.mocked(fundsApi.getFundDetail).mockImplementation(() => new Promise(() => { }))
            store.dispatch(fetchFundDetail('000001'))
            expect(store.getState().funds.loading).toBe(true)
            expect(store.getState().funds.error).toBeNull()
        })

        it('should set selectedFund and loading=false when fulfilled', async () => {
            vi.mocked(fundsApi.getFundDetail).mockResolvedValue({
                success: true,
                data: mockFund,
                error: null
            } as any)
            await store.dispatch(fetchFundDetail('000001'))
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.selectedFund).toEqual(mockFund)
        })

        it('should set error and loading=false when rejected', async () => {
            vi.mocked(fundsApi.getFundDetail).mockRejectedValue(new Error('Failed to fetch fund detail'))
            await store.dispatch(fetchFundDetail('000001'))
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.error).toBeTruthy()
        })
    })

    describe('fetchAllFunds thunk', () => {
        it('should set loading=true when pending', () => {
            vi.mocked(fundsApi.getAllFunds).mockImplementation(() => new Promise(() => { }))
            store.dispatch(fetchAllFunds())
            expect(store.getState().funds.loading).toBe(true)
            expect(store.getState().funds.error).toBeNull()
        })

        it('should set list and loading=false when fulfilled', async () => {
            vi.mocked(fundsApi.getAllFunds).mockResolvedValue({
                success: true,
                data: mockFunds,
                error: null
            } as any)
            await store.dispatch(fetchAllFunds())
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.list).toEqual(mockFunds)
        })

        it('should set error and loading=false when rejected', async () => {
            vi.mocked(fundsApi.getAllFunds).mockRejectedValue(new Error('Failed to fetch all funds'))
            await store.dispatch(fetchAllFunds())
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.error).toBeTruthy()
        })
    })

    describe('fetchFundById thunk', () => {
        it('should set loading=true when pending', () => {
            vi.mocked(fundsApi.getFundById).mockImplementation(() => new Promise(() => { }))
            store.dispatch(fetchFundById(1))
            expect(store.getState().funds.loading).toBe(true)
            expect(store.getState().funds.error).toBeNull()
        })

        it('should set selectedFund and loading=false when fulfilled', async () => {
            vi.mocked(fundsApi.getFundById).mockResolvedValue({
                success: true,
                data: mockFund,
                error: null
            } as any)
            await store.dispatch(fetchFundById(1))
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.selectedFund).toEqual(mockFund)
        })

        it('should set error and loading=false when rejected', async () => {
            vi.mocked(fundsApi.getFundById).mockRejectedValue(new Error('Failed to fetch fund by id'))
            await store.dispatch(fetchFundById(1))
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.error).toBeTruthy()
        })
    })

    describe('updateFund thunk', () => {
        const updateData: FundUpdateRequest = {
            market: 'SH',
            category: 'MIXED',
            description: 'Updated description',
            collectionFrequency: 30,
            dataSource: 'SINA'
        }

        it('should set loading=true when pending', () => {
            vi.mocked(fundsApi.updateFund).mockImplementation(() => new Promise(() => { }))
            store.dispatch(updateFund({ id: 1, data: updateData }))
            expect(store.getState().funds.loading).toBe(true)
            expect(store.getState().funds.error).toBeNull()
        })

        it('should update fund in list and set selectedFund when fulfilled', async () => {
            const updatedFund = { ...mockFund, ...updateData }
            vi.mocked(fundsApi.updateFund).mockResolvedValue({
                success: true,
                data: updatedFund,
                error: null
            } as any)

            // Pre-populate list
            store = configureStore({
                reducer: {
                    funds: fundsReducer
                },
                preloadedState: {
                    funds: {
                        list: [mockFund],
                        selectedFund: null,
                        loading: false,
                        error: null
                    }
                }
            })

            await store.dispatch(updateFund({ id: 1, data: updateData }))
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.list[0]).toEqual(updatedFund)
            expect(state.selectedFund).toEqual(updatedFund)
        })

        it('should set error and loading=false when rejected', async () => {
            vi.mocked(fundsApi.updateFund).mockRejectedValue(new Error('Failed to update fund'))
            await store.dispatch(updateFund({ id: 1, data: updateData }))
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.error).toBeTruthy()
        })
    })

    describe('deleteFund thunk', () => {
        it('should set loading=true when pending', () => {
            vi.mocked(fundsApi.deleteFund).mockImplementation(() => new Promise(() => { }))
            store.dispatch(deleteFund(1))
            expect(store.getState().funds.loading).toBe(true)
            expect(store.getState().funds.error).toBeNull()
        })

        it('should remove fund from list and clear selectedFund when fulfilled', async () => {
            vi.mocked(fundsApi.deleteFund).mockResolvedValue({
                success: true,
                data: 'Deleted',
                error: null
            } as any)

            // Pre-populate store with funds
            store = configureStore({
                reducer: {
                    funds: fundsReducer
                },
                preloadedState: {
                    funds: {
                        list: [mockFund, mockFund2],
                        selectedFund: mockFund,
                        loading: false,
                        error: null
                    }
                }
            })

            await store.dispatch(deleteFund(1))
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.list).toHaveLength(1)
            expect(state.list[0].id).toBe(2)
            expect(state.selectedFund).toBeNull()
        })

        it('should set error and loading=false when rejected', async () => {
            vi.mocked(fundsApi.deleteFund).mockRejectedValue(new Error('Failed to delete fund'))
            await store.dispatch(deleteFund(1))
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.error).toBeTruthy()
        })
    })

    describe('activateFund thunk', () => {
        it('should set loading=true when pending', () => {
            vi.mocked(fundsApi.activateFund).mockImplementation(() => new Promise(() => { }))
            store.dispatch(activateFund(1))
            expect(store.getState().funds.loading).toBe(true)
            expect(store.getState().funds.error).toBeNull()
        })

        it('should update fund in list and selectedFund when fulfilled', async () => {
            const activatedFund = { ...mockFund2, active: true }
            vi.mocked(fundsApi.activateFund).mockResolvedValue({
                success: true,
                data: activatedFund,
                error: null
            } as any)

            // Pre-populate store
            store = configureStore({
                reducer: {
                    funds: fundsReducer
                },
                preloadedState: {
                    funds: {
                        list: [mockFund, mockFund2],
                        selectedFund: mockFund2,
                        loading: false,
                        error: null
                    }
                }
            })

            await store.dispatch(activateFund(2))
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.list[1].active).toBe(true)
            expect(state.selectedFund?.active).toBe(true)
        })

        it('should set error and loading=false when rejected', async () => {
            vi.mocked(fundsApi.activateFund).mockRejectedValue(new Error('Failed to activate fund'))
            await store.dispatch(activateFund(1))
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.error).toBeTruthy()
        })
    })

    describe('deactivateFund thunk', () => {
        it('should set loading=true when pending', () => {
            vi.mocked(fundsApi.deactivateFund).mockImplementation(() => new Promise(() => { }))
            store.dispatch(deactivateFund(1))
            expect(store.getState().funds.loading).toBe(true)
            expect(store.getState().funds.error).toBeNull()
        })

        it('should update fund in list and selectedFund when fulfilled', async () => {
            const deactivatedFund = { ...mockFund, active: false }
            vi.mocked(fundsApi.deactivateFund).mockResolvedValue({
                success: true,
                data: deactivatedFund,
                error: null
            } as any)

            // Pre-populate store
            store = configureStore({
                reducer: {
                    funds: fundsReducer
                },
                preloadedState: {
                    funds: {
                        list: [mockFund, mockFund2],
                        selectedFund: mockFund,
                        loading: false,
                        error: null
                    }
                }
            })

            await store.dispatch(deactivateFund(1))
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.list[0].active).toBe(false)
            expect(state.selectedFund?.active).toBe(false)
        })

        it('should set error and loading=false when rejected', async () => {
            vi.mocked(fundsApi.deactivateFund).mockRejectedValue(new Error('Failed to deactivate fund'))
            await store.dispatch(deactivateFund(1))
            const state = store.getState().funds
            expect(state.loading).toBe(false)
            expect(state.error).toBeTruthy()
        })
    })

    describe('Synchronous reducers', () => {
        it('setSelectedFund should update selectedFund', () => {
            store.dispatch(setSelectedFund(mockFund))
            expect(store.getState().funds.selectedFund).toEqual(mockFund)
        })

        it('clearError should set error to null', () => {
            // First trigger an error
            store = configureStore({
                reducer: {
                    funds: fundsReducer
                },
                preloadedState: {
                    funds: {
                        list: [],
                        selectedFund: null,
                        loading: false,
                        error: 'Some error'
                    }
                }
            })

            store.dispatch(clearError())
            expect(store.getState().funds.error).toBeNull()
        })
    })
})
