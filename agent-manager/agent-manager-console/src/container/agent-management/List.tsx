
import * as React from 'react';
import * as actions from '../../actions';
import './index.less';
import '../../index.less';
import { Modal, Button, Input, Transfer, Tooltip, Select } from 'antd';
import { CustomBreadcrumb, CommonHead } from '../../component/CustomComponent';
import { getAgentListColumns, agentBreadcrumb, getQueryFormColumns } from './config';
import { IAgentHostVo, IAgentHostParams, IAgentVersion, IService, IAgentQueryFormColumns, IAgentHostSet, IOperationTasksParams } from '../../interface/agent';
import { agentHead, empty } from '../../constants/common';
import { getAgent, getAgentVersion, getServices, getTaskExists, createOperationTasks } from '../../api/agent';
import { connect } from "react-redux";
import { BasicTable } from 'antd-advanced';
import { regIp } from '../../constants/reg';
import { Dispatch } from 'redux';
import { findDOMNode } from 'react-dom';


const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any) => dispatch(actions.setModalId(modalId, params)),
  setDrawerId: (drawerId: string, params?: any) => dispatch(actions.setDrawerId(drawerId, params)),
});

type Props = ReturnType<typeof mapDispatchToProps>;
@connect(null, mapDispatchToProps)
export class AgentList extends React.Component<Props> {
  public versionRef = null as any;
  public healthRef = null as any;
  public containerRef = null as any;

  constructor(props: any) {
    super(props)
    this.versionRef = React.createRef();
    this.healthRef = React.createRef();
    this.containerRef = React.createRef();
  }

  public state = {
    loading: true,
    selectedRowKeys: [],
    includedInstalled: false,
    noInstalledHosts: [] as IAgentHostSet[],
    agentIds: [] as number[],
    includednoInstalled: false,
    installedHosts: [] as IAgentHostSet[],
    agentList: [],
    agentVersionList: [],
    servicesList: [],
    hidefer: false,
    targetKeys: [],
    applyInput: '',
    direction: 'left',
    total: 0,
    form: '',
    queryParams: {
      pageNo: 1,
      pageSize: 20,
      agentHealthLevelList: [],
      agentVersionIdList: [],
      containerList: [],
      hostCreateTimeEnd: empty,
      hostCreateTimeStart: empty,
      serviceIdList: [],
      hostName: empty,
      ip: empty,
    } as IAgentHostParams,
  }

  public filterApplyOption = (inputValue: any, option: any) => {
    return option.servicename.indexOf(inputValue) > -1;
  }

  public onApplyChange = (targetKeys: any, direction: any) => {
    this.setState({ applyInput: `?????????${targetKeys?.length}???`, hidefer: true, targetKeys, direction, });
  };

  public onApplySearch = (dir: any, value: any) => {
    // console.log('search:', dir, value);
  };

  public onInputClick = () => {
    const { targetKeys, direction, servicesList } = this.state;
    let keys = [] as number[];
    if (direction === 'right') {
      keys = targetKeys;
    } else if (direction === 'left') {
      if (targetKeys?.length === servicesList?.length) {
        keys = [];
      } else {
        keys = targetKeys;
      }
    }
    this.setState({
      applyInput: keys?.length ? `?????????${keys?.length}???` : '',
      hidefer: true,
      targetKeys: keys,
    });
  }

  public handleClickOutside(e: any) {
    // ??????????????????????????????????????????div???
    let result = findDOMNode(this.refs.refTest)?.contains(e.target);
    if (!result) {
      this.setState({ hidefer: false });
    }
  }

  public queryFormColumns() {
    const { agentVersionList, servicesList, targetKeys, hidefer, applyInput, form } = this.state;
    const applyObj = {
      type: 'custom',
      title: '????????????',
      dataIndex: 'serviceIdList',
      component: (
        <div className='apply-box'>
          <Select
            // mode="multiple"
            placeholder='?????????'
            // ref={containerRef}
            allowClear={true}
            showArrow={true}
          // onInputKeyDown={() => {
          //   form.resetFields(['containerList']);
          //   containerRef.current.blur();
          // }}
          // maxTagCount={0}
          // maxTagPlaceholder={(values) => values?.length ? `?????????${values?.length}???` : '?????????'}
          >
            {servicesList.map((d: any, index) => {
              return <Select.Option value={d.id} key={index}>{d.servicename}</ Select.Option>
            })}
          </Select>
          {/* <Input value={applyInput} placeholder='?????????' onClick={this.onInputClick} />
          {hidefer && <div className='apply' ref="refTest">
            <Transfer
              dataSource={servicesList}
              showSearch
              filterOption={this.filterApplyOption}
              targetKeys={targetKeys}
              onChange={this.onApplyChange}
              onSearch={this.onApplySearch}
              render={item => item.servicename}
              className="customTransfer"
            />
          </div>} */}
        </div>
      ),
    };
    const queryColumns = getQueryFormColumns(agentVersionList, this.versionRef, this.healthRef, this.containerRef, form);
    queryColumns.splice(3, 0, applyObj);
    return queryColumns;
  }

  public setServiceIdList = () => {
    const { servicesList, targetKeys } = this.state;
    const serviceIds = [] as number[];
    servicesList.map((ele: IService, index) => {
      if (targetKeys?.includes(index as never)) {
        serviceIds.push(ele.id);
      }
    });
    return serviceIds;
  }

  public onChangeParams = (values: IAgentQueryFormColumns, form: any) => {  // ????????????????????????????????????
    const { pageNo, pageSize } = this.state.queryParams;
    let ip = '';
    let hostName = '';
    if (new RegExp(regIp).test(values?.hostNameOrIp)) {
      ip = values?.hostNameOrIp || '';
    } else {
      hostName = values?.hostNameOrIp || '';
    }
    this.setState({
      form,
      queryParams: {
        pageNo,
        pageSize,
        agentHealthLevelList: values?.agentHealthLevelList || [],
        agentVersionIdList: values?.agentVersionIdList ? [values.agentVersionIdList] : [],
        containerList: values?.containerList ? [values?.containerList] : [],
        hostCreateTimeStart: values?.hostCreateTime?.length ? values?.hostCreateTime[0]?.valueOf() : '',
        hostCreateTimeEnd: values?.hostCreateTime?.length ? values?.hostCreateTime[1]?.valueOf() : '',
        serviceIdList: this.setServiceIdList(),
        hostName,
        ip,
      } as IAgentHostParams,
    })
  }

  public onSearchParams = () => {  // ???????????????????????????
    const { queryParams, targetKeys } = this.state;
    if (targetKeys?.length > 0) {
      queryParams.serviceIdList = this.setServiceIdList();
    } else {
      queryParams.serviceIdList = []
      this.setState({
        targetKeys: [],
        applyInput: '',
        direction: 'left',
      });
    }
    this.getAgentData(this.state.queryParams);
  }

  public onResetParams = () => {
    const resetParams = {
      pageNo: 1,
      pageSize: 20,
      agentHealthLevelList: [],
      agentVersionIdList: [],
      containerList: [],
      hostCreateTimeEnd: empty,
      hostCreateTimeStart: empty,
      serviceIdList: [],
      hostName: empty,
      ip: empty,
    } as IAgentHostParams;
    this.setState({
      queryParams: resetParams,
      targetKeys: [],
      applyInput: '',
      direction: 'left',
    });
    this.getAgentData(resetParams);
  }

  public getData = () => {
    this.setState({
      selectedRowKeys: [],
      includedInstalled: false,
      noInstalledHosts: [],
      agentIds: [],
      includednoInstalled: false,
      installedHosts: [],
    });
    this.getAgentData(this.state.queryParams)
  }

  public agentListColumns() {
    const getData = () => this.getData();
    const columns = getAgentListColumns(this.props.setModalId, this.props.setDrawerId, getData);
    return columns;
  }

  public handleNewHost = () => {
    this.props.setModalId('NewHost', {
      cb: () => this.getAgentData(this.state.queryParams),
    });
  }

  public handleHost = (taskType: number, hosts: IAgentHostSet[]) => {
    this.props.setModalId('InstallHost', {
      taskType,
      hosts,
      cb: () => this.getData(),
    });
  }

  public handleInstallHost = () => { // ?????? 0
    const { includedInstalled, noInstalledHosts } = this.state;
    if (!noInstalledHosts?.length) { return Modal.confirm({ title: `????????????????????????????????????` }); }
    if (includedInstalled) {
      return Modal.confirm({
        title: `???????????????Agent????????????????????????????????????`,
        onOk: () => this.handleHost(0, noInstalledHosts),
      });
    }
    return this.handleHost(0, noInstalledHosts);
  }

  public handleUpgradeHost = () => { // ?????? 2
    const { installedHosts, includednoInstalled } = this.state;
    if (!installedHosts?.length) { return Modal.confirm({ title: `????????????????????????????????????` }); }
    if (includednoInstalled) {
      return Modal.confirm({
        title: `???????????????Agent????????????????????????????????????`,
        onOk: () => this.openUpgradeHost(),
      });
    }
    return this.openUpgradeHost();
  }

  public openUpgradeHost = () => {
    const { agentIds, installedHosts } = this.state;
    getTaskExists(JSON.stringify(agentIds)).then((res: boolean) => {
      if (res) {
        return Modal.confirm({
          title: <a className='fail'>??????agent??????????????????????????????????????????????????????????????????????????????</a>,
          onOk: () => this.handleHost(2, installedHosts),
        });
      }
      return this.handleHost(2, installedHosts);
    }).catch((err: any) => {
      // console.log(err);
    });
  }

  public handleUninstallHost = () => { // ?????? 1
    const { installedHosts, includednoInstalled } = this.state;
    if (!installedHosts?.length) { return Modal.confirm({ title: `????????????????????????????????????` }); }
    if (includednoInstalled) {
      return Modal.confirm({
        title: `???????????????Agent????????????????????????????????????`,
        onOk: () => this.openUninstallHost(),
      });
    }
    return this.openUninstallHost();
  }

  public openUninstallHost = () => {
    const { agentIds } = this.state;
    getTaskExists(JSON.stringify(agentIds)).then((res: boolean) => {
      if (res) {
        return Modal.confirm({
          title: <a className='fail'>??????agent??????????????????????????????????????????????????????????????????????????????</a>,
          onOk: () => this.uninstallHostModal(res),
        });
      }
      return this.uninstallHostModal(res);
    }).catch((err: any) => {
      // console.log(err);
    });
  }

  public uninstallHostModal = (check: boolean) => {
    Modal.confirm({
      title: `??????????????????Agent??????`,
      content: <a className="fail">?????????????????????????????????????????????</a>,
      onOk: () => this.uninstallHost(check),
    });
  }

  public uninstallHost = (check: boolean) => {
    const { agentIds, installedHosts } = this.state;
    const params = {
      agentIds,
      checkAgentCompleteCollect: check ? 1 : 0, // 1?????? 0 ?????????
      agentVersionId: empty,
      hostIds: [],
      taskType: 1,
    } as IOperationTasksParams;
    createOperationTasks(params).then((res: number) => {
      Modal.success({
        title: <><a href="/agent/operationTasks">{agentIds?.length > 1 ? '??????' : installedHosts[0]?.hostName}??????Agent??????(??????ID???{res}???</a>???????????????</>,
        content: '?????????????????????????????????Agent?????????>????????????????????????????????????',
        okText: '??????',
        onOk: () => {
          this.setState({ selectedRowKeys: [] });
          this.getAgentData(this.state.queryParams);
        },
      });
    }).catch((err: any) => {
      // console.log(err);
    });
  }

  public onSelectChange = (selectedRowKeys: any, selectedRows: IAgentHostSet[]) => {
    const noInstalledHosts = selectedRows?.filter((d: any) => !d.agentId); // ?????????????????????agent
    const installedHosts = selectedRows?.filter((d: any) => d.agentId); // ?????????????????????agent
    this.setState({
      selectedRowKeys,
      includedInstalled: noInstalledHosts?.length < selectedRows?.length, // ???????????????agent
      noInstalledHosts,
      includednoInstalled: installedHosts?.length < selectedRows?.length, // ???????????????agent
      installedHosts, // 0:?????? 1:?????? 2:??????
      agentIds: installedHosts.map(ele => ele.agentId),
    });
  };

  public onPageChange = (current: number, size: number | undefined) => {
    const { queryParams } = this.state;
    const pageParams = {
      // pageNo: current,
      // pageSize: size,
      agentHealthLevelList: queryParams?.agentHealthLevelList || [],
      agentVersionIdList: queryParams?.agentVersionIdList || [],
      containerList: queryParams?.containerList || [],
      hostCreateTimeStart: queryParams?.hostCreateTimeStart,
      hostCreateTimeEnd: queryParams?.hostCreateTimeEnd,
      serviceIdList: this.setServiceIdList(),
      hostName: queryParams?.hostName,
      ip: queryParams?.ip,
    } as IAgentHostParams;
    this.setState({ queryParams: pageParams });
    this.getAgentData(pageParams, current, size);
  }

  public getAgentData = (queryParams: IAgentHostParams, current?: number, size?: number) => {
    queryParams.pageNo = current || 1;
    queryParams.pageSize = size || 20;
    this.setState({ loading: true });
    getAgent(queryParams).then((res: IAgentHostVo) => {
      const data = res?.resultSet?.map((ele: IAgentHostSet, index: number) => { return { key: index, ...ele } });
      this.setState({
        loading: false,
        agentList: data,
        total: res.total,
      });
    }).catch((err: any) => {
      this.setState({ loading: false });
    });
  }

  public getAgentVersionData = () => {
    getAgentVersion().then((res: IAgentVersion[]) => {
      this.setState({ agentVersionList: res });
    }).catch((err: any) => {
      // console.log(err);
    });
  }

  public getServicesData = () => {
    getServices().then((res: IService[]) => {
      const data = res.map((ele, index) => {
        return { ...ele, key: index, };
      });
      this.setState({
        servicesList: data,
      })
    }).catch((err: any) => {
      // console.log(err);
    });
  }

  public componentWillUnmount = () => {
    this.setState = () => { return };
    document.removeEventListener('mousedown', (e) => this.handleClickOutside(e), false);
  }

  public componentDidMount() {
    this.getAgentData(this.state.queryParams);
    this.getAgentVersionData();
    this.getServicesData();
    document.addEventListener('mousedown', (e) => this.handleClickOutside(e), false);
  }

  public render() {
    const { loading, total, agentList, selectedRowKeys } = this.state;
    const { pageNo, pageSize } = this.state.queryParams;
    const rowSelection = {
      selectedRowKeys,
      onChange: this.onSelectChange,
    };
    return (
      <>
        <CustomBreadcrumb btns={agentBreadcrumb} />
        <div className="list page-wrapper agent-list">
          <CommonHead heads={agentHead} />
          <BasicTable
            rowKey='hostId'
            showReloadBtn={false}
            // showQueryCollapseButton={true}
            loading={loading}
            reloadBtnPos="left"
            reloadBtnType="btn"
            filterType="none"
            hideContentBorder={true}
            showSearch={false}
            columns={this.agentListColumns()}
            rowSelection={rowSelection}
            dataSource={agentList}
            isQuerySearchOnChange={false}
            queryFormColumns={this.queryFormColumns()}
            showQueryCollapseButton={this.queryFormColumns().length > 2 ? true : false}
            pagination={{
              current: pageNo,
              pageSize: pageSize,
              total,
              showQuickJumper: true,
              showSizeChanger: true,
              pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
              onChange: (current, size) => this.onPageChange(current, size),
              onShowSizeChange: (current, size) => this.onPageChange(current, size),
              showTotal: () => `??? ${total} ???`,
            }}
            queryFormProps={{
              searchText: '??????',
              resetText: '??????',
              onChange: this.onChangeParams,
              onSearch: this.onSearchParams,
              onReset: this.onResetParams,
            }}
            customHeader={
              <div className="table-button">
                <p>
                  <Tooltip placement="top" title="??????????????????????????????">
                    <Button disabled onClick={this.handleInstallHost}>??????</Button>
                  </Tooltip>
                  <Tooltip placement="top" title="??????????????????????????????">
                    <Button disabled onClick={this.handleUpgradeHost}>??????</Button>
                  </Tooltip>
                  <Tooltip placement="top" title="??????????????????????????????">
                    <Button disabled onClick={this.handleUninstallHost}>??????</Button>
                  </Tooltip>
                </p>
                <Button type="primary" onClick={this.handleNewHost}>????????????</Button>
              </div>
            }
          />
        </div>
      </>
    );
  }
};
