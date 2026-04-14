import { Modal, Button, Result, List, Typography, Divider } from 'antd'
import { CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons'
import type { BatchCreateAlertResponse } from '@services/api/alerts'

const { Text } = Typography

export interface BatchResult {
  success: boolean
  data: BatchCreateAlertResponse | null
  error: string | null
}

interface BatchResultModalProps {
  visible: boolean
  batchResult: BatchResult | null
  onClose: () => void
  onRetryFailed: () => void
  onGoToList: () => void
}

export const BatchResultModal = ({
  visible,
  batchResult,
  onClose,
  onRetryFailed,
  onGoToList
}: BatchResultModalProps) => {
  const data = batchResult?.data

  const renderContent = () => {
    if (!data) return null

    const { totalCount, successCount, failureCount, successList, failureList } = data

    return (
      <div>
        <Result
          status={failureCount === 0 ? 'success' : failureCount === totalCount ? 'error' : 'warning'}
          title={
            failureCount === 0
              ? `全部成功！成功创建 ${successCount} 条提醒`
              : failureCount === totalCount
                ? `全部失败，共 ${failureCount} 条`
                : `部分成功：成功 ${successCount} 条，失败 ${failureCount} 条`
          }
        />

        <Divider orientation="left">成功列表 ({successCount})</Divider>
        <List
          size="small"
          dataSource={successList}
          renderItem={(item) => (
            <List.Item>
              <CheckCircleOutlined style={{ color: '#52c41a', marginRight: 8 }} />
              <Text strong>{item.symbol}</Text>
              <Text style={{ marginLeft: 8 }}>{item.symbolName}</Text>
            </List.Item>
          )}
        />

        {failureCount > 0 && (
          <>
            <Divider orientation="left">失败列表 ({failureCount})</Divider>
            <List
              size="small"
              dataSource={failureList}
              renderItem={(item) => (
                <List.Item>
                  <CloseCircleOutlined style={{ color: '#ff4d4f', marginRight: 8 }} />
                  <Text strong>{item.symbol}</Text>
                  <Text style={{ marginLeft: 8 }}>{item.symbolName}</Text>
                  <Text type="danger" style={{ marginLeft: 8 }}>
                    原因: <span>{item.reason}</span>
                  </Text>
                </List.Item>
              )}
            />
          </>
        )}
      </div>
    )
  }

  const isAllSuccess = data && data.failureCount === 0

  return (
    <Modal
      title="批量创建结果"
      open={visible}
      onCancel={onClose}
      footer={[
        data && data.failureCount > 0 && (
          <Button key="retry" type="primary" danger onClick={onRetryFailed}>
            重试失败项 ({data.failureCount})
          </Button>
        ),
        <Button
          key="back"
          type="primary"
          onClick={onGoToList}
        >
          {isAllSuccess ? '完成' : '返回列表'}
        </Button>
      ]}
      width={720}
    >
      {renderContent()}
    </Modal>
  )
}
