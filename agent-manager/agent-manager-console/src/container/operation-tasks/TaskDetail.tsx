
import * as React from 'react';
import { CustomBreadcrumb, DescriptionsItems } from '../../component/CustomComponent';
import { taskDetailBreadcrumb } from './config';
import { IBaseInfo } from '../../interface/common';
import { TaskTable } from './TaskTable';
import { Tag } from 'antd';
import { IAgentHostSet } from '../../interface/agent';
import './index.less';
import { getTaskDetail } from '../../api/operationTasks';
import moment from 'moment';
import { timeFormat } from '../../constants/time';

interface Props {
  location: any;
}
export class TaskDetail extends React.Component<Props> {
  public state = {
    hostDetail: {} as IAgentHostSet,
    agentOperationTaskExecuteDetailVOList: [],
    loading: false,
    subtitle: '',
    total: {
      execution: 0,
      failed: 0,
      pending: 0,
      successful: 0,
      total: 0
    },
    taskDetail: {
      agentOperationTaskName: '',
      agentOperationTaskStatus: 9,
      agentOperationTaskEndTime: '',
      agentOperationTaskId: '',
      agentOperationTaskStartTime: '',
      agentOperationTaskType: 9,
      agentVersion: "",
      operator: "",
      relationHostCount: ''
    }
  }

  constructor(props: any) {
    super(props);
  }

  public getOperationTaskList = (agentOperationTaskId: any) => {
    getTaskDetail(agentOperationTaskId).then((res: any) => {
      this.setState({
        agentOperationTaskExecuteDetailVOList: res.agentOperationTaskExecuteDetailListVO.agentOperationTaskExecuteDetailVOList?.map((item: any, index: number) => {
          return {
            key: index, ...item
          }
        }),
        total: { ...res.agentOperationTaskExecuteDetailListVO },
        taskDetail: { ...res },
        loading: false,
      });
    }).catch((err: any) => {
      this.setState({ loading: false });
    });
  }

  public componentDidMount() {
    this.getOperationTaskList(this.props.location.state.agentOperationTaskId);
  }

  public render() {
    const { agentOperationTaskStatus, agentOperationTaskType } = this.state.taskDetail;
    this.state.subtitle = agentOperationTaskStatus === 100 ? "green-tag" : agentOperationTaskStatus === 30 ? "blue-tag" : '';
    const taskDetailBaseInfo: IBaseInfo[] = [{
      label: '????????????ID',
      key: 'agentOperationTaskId',
    }, {
      label: '????????????',
      key: 'agentOperationTaskType',
      render: (text: any) => {
        return text == 2 ? '??????' : text == 0 ? '??????' : text == 1 ? '??????' : ''
      }
    }, {
      label: '?????????',
      key: 'operator',
    }, {
      label: '????????????',
      key: 'agentOperationTaskStartTime',
      render: (t: number) => t ? moment(t).format(timeFormat) : '',
    }, {
      label: '????????????',
      key: 'agentOperationTaskEndTime',
      render: (t: number) => t ? moment(t).format(timeFormat) : '',
    }, {
      label: 'Agent?????????',
      key: 'agentVersion',
      invisible: agentOperationTaskType == 1 ? true : false
    }];
    return (
      <>
        <CustomBreadcrumb btns={taskDetailBreadcrumb} />{agentOperationTaskStatus}
        <DescriptionsItems
          title={agentOperationTaskType == 0 ? '??????Agent' : agentOperationTaskType == 2 ? '??????Agent' : agentOperationTaskType == 1 ? '??????Agent' : ''}
          column={5}
          loading={this.state.loading}
          subTitle={(<Tag className={this.state.subtitle}>{agentOperationTaskStatus === 100 ? '??????' : agentOperationTaskStatus === 30 ? '?????????' : ''}</Tag>)}
          baseInfo={taskDetailBaseInfo}
          baseData={this.state.taskDetail}
          className='taskDetailBaseInfo'
        />
        <div className="detail-wrapper">
          <TaskTable agentOperationTaskId={this.props.location.state.agentOperationTaskId} detailList={this.state.agentOperationTaskExecuteDetailVOList} total={this.state.total} />
        </div>
      </>
    );
  }
}