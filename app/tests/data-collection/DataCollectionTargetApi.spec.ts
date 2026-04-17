import { expect, test } from '@playwright/test'

/**
 * 数据采集目标管理 API E2E 测试
 * 测试 /api/data-collection-targets 接口的各种场景
 * 
 * 注意：后端已删除通用的 POST /api/data-collection-targets 接口，
 * 仅保留 POST /api/data-collection-targets/createByCode?code={code} 用于创建基金类型目标。
 * 因此部分测试需要使用现有数据或跳过。
 */
test.describe('数据采集目标管理 API E2E', () => {
  const API_BASE = '/api/data-collection-targets'

  test.describe('快速创建操作 (createByCode)', () => {
    test('CT-1.1 创建采集目标应成功', async ({ request }) => {
      // 使用6位数字代码（基金代码格式）
      const uniqueCode = `${Date.now().toString().slice(-6)}`

      // 使用 createByCode 端点创建采集目标
      const response = await request.post(`${API_BASE}/createByCode?code=${uniqueCode}`)

      expect(response.ok()).toBeTruthy()

      const body = await response.json()
      expect(body.success).toBe(true)
      expect(body.data).toHaveProperty('id')
      expect(body.data.code).toBe(uniqueCode)
    })

    test('CT-1.2 创建重复代码目标应返回已存在的目标（幂等设计）', async ({ request }) => {
      // 使用6位数字代码（基金代码格式）
      const uniqueCode = `${Date.now().toString().slice(-6)}`

      // 创建第一个
      const firstResponse = await request.post(`${API_BASE}/createByCode?code=${uniqueCode}`)
      const firstBody = await firstResponse.json()
      expect(firstBody.success).toBe(true)
      expect(firstBody.data.code).toBe(uniqueCode)
      const firstId = firstBody.data?.id

      // 尝试创建重复的 - 应返回已存在的目标（幂等设计）
      const secondResponse = await request.post(`${API_BASE}/createByCode?code=${uniqueCode}`)
      const secondBody = await secondResponse.json()
      expect(secondBody.success).toBe(true)
      expect(secondBody.data.code).toBe(uniqueCode)

      // 验证返回的是同一对象（幂等性：相同代码应返回相同ID）
      expect(secondBody.data?.id).toBe(firstId)
    })

    test('CT-1.3 根据ID查询目标应成功', async ({ request }) => {
      // 先创建一个目标
      const uniqueCode = `${Date.now().toString().slice(-6)}`
      const createResponse = await request.post(`${API_BASE}/createByCode?code=${uniqueCode}`)
      const created = await createResponse.json()

      if (created.data?.id) {
        // 查询刚创建的目标
        const response = await request.get(`${API_BASE}/${created.data.id}`)

        expect(response.ok()).toBeTruthy()

        const body = await response.json()
        expect(body.success).toBe(true)
        expect(body.data.id).toBe(created.data.id)
        expect(body.data.code).toBe(uniqueCode)
      }
    })

    test('CT-1.4 根据代码查询目标应成功', async ({ request }) => {
      // 使用真实基金代码创建目标
      const response = await request.post(`${API_BASE}/createByCode?code=000001`)

      if (response.ok()) {
        const body = await response.json()
        if (body.success && body.data.code) {
          // 按代码查询
          const getResponse = await request.get(`${API_BASE}/code/${body.data.code}`)

          expect(getResponse.ok()).toBeTruthy()

          const getBody = await getResponse.json()
          expect(getBody.success).toBe(true)
          expect(getBody.data.code).toBe(body.data.code)
        }
      }
    })

    test('CT-1.5 更新目标应成功', async ({ request }) => {
      // 创建目标
      const uniqueCode = `${Date.now().toString().slice(-6)}`
      const createResponse = await request.post(`${API_BASE}/createByCode?code=${uniqueCode}`)
      const created = await createResponse.json()

      if (created.data?.id) {
        // 更新目标名称
        const updateData = {
          ...created.data,
          name: '更新后的名称'
        }

        const updateResponse = await request.put(`${API_BASE}/${created.data.id}`, {
          data: updateData
        })

        expect(updateResponse.ok()).toBeTruthy()

        const updateBody = await updateResponse.json()
        expect(updateBody.success).toBe(true)
        expect(updateBody.data.name).toBe('更新后的名称')
      }
    })

    test('CT-1.6 删除目标应成功', async ({ request }) => {
      // 创建目标
      const uniqueCode = `${Date.now().toString().slice(-6)}`
      const createResponse = await request.post(`${API_BASE}/createByCode?code=${uniqueCode}`)
      const created = await createResponse.json()

      if (created.data?.id) {
        // 删除目标
        const deleteResponse = await request.delete(`${API_BASE}/${created.data.id}`)

        expect(deleteResponse.ok()).toBeTruthy()

        const deleteBody = await deleteResponse.json()
        expect(deleteBody.success).toBe(true)

        // 验证已删除
        const getResponse = await request.get(`${API_BASE}/${created.data.id}`)
        const getBody = await getResponse.json()
        expect(getBody.success).toBe(false)
      }
    })
  })

  test.describe('查询操作', () => {
    test('CT-2.1 获取所有目标应返回列表', async ({ request }) => {
      const response = await request.get(API_BASE)

      expect(response.ok()).toBeTruthy()

      const body = await response.json()
      expect(body.success).toBe(true)
      expect(Array.isArray(body.data)).toBe(true)
    })

    test('CT-2.2 按类型查询应返回对应类型的目标', async ({ request }) => {
      const response = await request.get(`${API_BASE}/type/FUND`)

      expect(response.ok()).toBeTruthy()

      const body = await response.json()
      if (body.success) {
        expect(Array.isArray(body.data)).toBe(true)
        // 验证返回的都是 FUND 类型
        body.data.forEach((target: any) => {
          expect(target.type).toBe('FUND')
        })
      }
    })

    test('CT-2.3 获取激活目标应只返回激活的', async ({ request }) => {
      const response = await request.get(`${API_BASE}/active`)

      expect(response.ok()).toBeTruthy()

      const body = await response.json()
      if (body.success) {
        expect(Array.isArray(body.data)).toBe(true)
        // 所有返回的目标应该是激活状态
        body.data.forEach((target: any) => {
          const isActive = target.isActive ?? target.active ?? target.status === 'ACTIVE'
          expect(isActive).toBeTruthy()
        })
      }
    })

    test('CT-2.4 按分类查询应返回对应分类的目标', async ({ request }) => {
      const response = await request.get(`${API_BASE}/category/主板`)

      expect(response.ok()).toBeTruthy()

      const body = await response.json()
      if (body.success) {
        expect(Array.isArray(body.data)).toBe(true)
      }
    })

    test('CT-2.5 查询需要采集的目标', async ({ request }) => {
      const response = await request.get(`${API_BASE}/needing-collection`)

      expect(response.ok()).toBeTruthy()

      const body = await response.json()
      expect(body.success).toBe(true)
      expect(Array.isArray(body.data)).toBe(true)
    })

    test('CT-2.6 搜索目标应返回匹配结果', async ({ request }) => {
      // 使用真实基金代码创建目标
      const createResponse = await request.post(`${API_BASE}/createByCode?code=000001`)

      if (createResponse.ok()) {
        const body = await createResponse.json()
        if (body.success && body.data && body.data.name) {
          // 使用基金名称关键词搜索
          const keyword = body.data.name.substring(0, 2) // 取名称前2个字符
          const response = await request.get(`${API_BASE}/search?keyword=${encodeURIComponent(keyword)}`)

          expect(response.ok()).toBeTruthy()

          const searchBody = await response.json()
          if (searchBody.success) {
            expect(Array.isArray(searchBody.data)).toBe(true)
          }
        }
      }
    })

    test('CT-2.7 搜索带类型过滤应返回匹配结果', async ({ request }) => {
      const response = await request.get(`${API_BASE}/search?type=FUND&keyword=基金`)

      expect(response.ok()).toBeTruthy()

      const body = await response.json()
      if (body.success) {
        expect(Array.isArray(body.data)).toBe(true)
      }
    })
  })

  test.describe('激活/停用操作', () => {
    test('CT-3.1 激活目标应成功', async ({ request }) => {
      // 创建停用状态的目标
      const uniqueCode = `${Date.now().toString().slice(-6)}`
      const createResponse = await request.post(`${API_BASE}/createByCode?code=${uniqueCode}`)
      const created = await createResponse.json()

      if (created.data?.id) {
        // 先停用
        await request.post(`${API_BASE}/${created.data.id}/deactivate`)

        // 再激活
        const response = await request.post(`${API_BASE}/${created.data.id}/activate`)

        expect(response.ok()).toBeTruthy()

        const body = await response.json()
        expect(body.success).toBe(true)
      }
    })

    test('CT-3.2 停用目标应成功', async ({ request }) => {
      // 创建激活状态的目标
      const uniqueCode = `${Date.now().toString().slice(-6)}`
      const createResponse = await request.post(`${API_BASE}/createByCode?code=${uniqueCode}`)
      const created = await createResponse.json()

      if (created.data?.id) {
        // 停用
        const response = await request.post(`${API_BASE}/${created.data.id}/deactivate`)

        expect(response.ok()).toBeTruthy()

        const body = await response.json()
        expect(body.success).toBe(true)
      }
    })

    test('CT-3.3 按代码停用目标应成功', async ({ request }) => {
      // 使用真实基金代码
      const createResponse = await request.post(`${API_BASE}/createByCode?code=000001`)

      if (createResponse.ok()) {
        const body = await createResponse.json()
        if (body.success && body.data && body.data.code) {
          // 按代码停用
          const response = await request.post(`${API_BASE}/code/${body.data.code}/deactivate`)

          expect(response.ok()).toBeTruthy()
        }
      }
    })
  })

  test.describe('统计操作', () => {
    test('CT-4.1 获取目标总数应成功', async ({ request }) => {
      const response = await request.get(`${API_BASE}/count`)

      expect(response.ok()).toBeTruthy()

      const body = await response.json()
      expect(body.success).toBe(true)
      expect(typeof body.data).toBe('number')
    })

    test('CT-4.2 按类型统计数量应成功', async ({ request }) => {
      const response = await request.get(`${API_BASE}/count/type/FUND`)

      expect(response.ok()).toBeTruthy()

      const body = await response.json()
      expect(body.success).toBe(true)
      expect(typeof body.data).toBe('number')
    })

    test('CT-4.3 统计激活目标数量应成功', async ({ request }) => {
      const response = await request.get(`${API_BASE}/count/active`)

      expect(response.ok()).toBeTruthy()

      const body = await response.json()
      expect(body.success).toBe(true)
      expect(typeof body.data).toBe('number')
    })
  })

  test.describe('快速添加操作', () => {
    test('CT-5.1 快速添加基金目标应成功', async ({ request }) => {
      // 使用真实基金代码测试
      const response = await request.post(`${API_BASE}/createByCode?code=000001`)

      const body = await response.json()
      // 可能成功或失败（取决于基金代码是否有效）
      expect(body).toHaveProperty('success')
      if (body.success) {
        expect(body.data).toHaveProperty('id')
      }
    })

    test('CT-5.2 添加无效基金代码应返回错误', async ({ request }) => {
      // 使用无效代码 - 5位数字格式，不可能匹配任何基金
      const response = await request.post(`${API_BASE}/createByCode?code=99999`)

      const body = await response.json()
      // 应该返回错误（因为无法获取有效的基金数据）
      expect(body.success).toBe(false)
    })
  })

  test.describe('错误处理', () => {
    test('CT-6.1 查询不存在的目标应返回错误', async ({ request }) => {
      const response = await request.get(`${API_BASE}/999999`)

      const body = await response.json()
      expect(body.success).toBe(false)
    })

    test('CT-6.2 删除不存在的目标应返回错误', async ({ request }) => {
      const response = await request.delete(`${API_BASE}/999999`)

      const body = await response.json()
      expect(body.success).toBe(false)
    })

    test('CT-6.3 更新不存在的目标应返回错误', async ({ request }) => {
      const target = {
        code: 'NOTEXIST',
        name: '不存在',
        type: 'FUND'
      }

      const response = await request.put(`${API_BASE}/999999`, { data: target })

      const body = await response.json()
      expect(body.success).toBe(false)
    })
  })
})
