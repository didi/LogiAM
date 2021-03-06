import React, { useState, useEffect } from 'react';
import * as actions from '../../actions';
import { connect } from "react-redux";
import { Dispatch } from 'redux';
import { FormComponentProps } from 'antd/lib/form';
import { Select, Form, Input, Radio, Row, Col, Collapse, InputNumber } from 'antd';
import { collectLogTypes, codingFormatTypes, collectLogFormItemLayout } from './config';
import LoopAddLogFileType from './LoopAddLogFileType';
import LogRepeatForm from './LogRepeatForm';
import CatalogPathList from './CatalogPathList';
import { DownOutlined, UpOutlined } from '@ant-design/icons';
import { flowUnitList } from '../../constants/common';
import { ILabelValue } from '../../interface/common';
import './index.less';
import LogFileType from './LogFileType';



const { Panel } = Collapse;
interface ICollectLogProps extends FormComponentProps {
  collectLogType: string;
  logFilter: number;
  cataPathlist: string[];
  slicingRuleLog: number;
  filePathList: string[];
  slicingRuleLogList: number[];
  suffixfilesList: number[];
  hostNames: any;
  isNotLogPath: any;
  setisNotLogPath: any;
}

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setLogType: (logType: string) => dispatch(actions.setLogType(logType)),
});
type Props = ReturnType<typeof mapDispatchToProps>;
const CollectLogConfiguration = (props: Props & ICollectLogProps) => {
  const { getFieldDecorator, getFieldValue } = props.form;
  const editUrl = window.location.pathname.includes('/edit-task');
  const [collectLogType, setCollectLogType] = useState('file');
  const [logFilter, setLogFilter] = useState(0);
  const [activeKeys, setActiveKeys] = useState([] as string[]);
  // const initial = props?.addFileLog && !!Object.keys(props?.addFileLog)?.length;
  const [logListFile, setLogListFile] = useState([])
  // const [isNotLogPath, setisNotLogPath] = useState(false)

  const customPanelStyle = {
    border: 0,
    overflow: 'hidden',
    padding: '10px 0 0',
    background: '#fafafa',
  };

  const getCollectLogType = (e: string) => {
    setCollectLogType(e);
    props.setLogType(e);
  }

  const onLogFilterChange = (e: any) => {
    setLogFilter(e.target.value);
  }
  useEffect(() => {
    if (editUrl) {
      setCollectLogType(props.collectLogType);
      setLogFilter(props.logFilter);
    }
  }, [props.collectLogType, props.logFilter]);

  const collapseCallBack = (key: any) => {
    setActiveKeys(key);
  }

  return (
    <div className='set-up collect-log-config'>
      <Form {...collectLogFormItemLayout}>

        {/* <Form.Item label="??????????????????">
          {getFieldDecorator('step2_collectionLogType', {
            validateTrigger: ['onChange', 'onBlur'],
            initialValue: collectLogTypes[0]?.value,
            rules: [{ required: true, message: '???????????????????????????' }],
          })(
            <Select className="select" onChange={getCollectLogType}>
              {collectLogTypes.map(ele => {
                return (
                  <Select.Option key={ele.value} value={ele.value}>
                    {ele.label}
                  </Select.Option>
                );
              })}
            </Select>,
          )}
        </Form.Item> */}

        {/* <Form.Item label="????????????">
          {getFieldDecorator('step2_charset', {
            validateTrigger: ['onChange', 'onBlur'],
            initialValue: codingFormatTypes[0]?.value,
            rules: [{ required: true, message: '?????????????????????' }],
          })(
            <Select className="select">
              {codingFormatTypes.map(ele => {
                return (
                  <Select.Option key={ele.value} value={ele.value}>
                    {ele.label}
                  </Select.Option>
                );
              })}
            </Select>,
          )}
        </Form.Item> */}


        <LoopAddLogFileType
          form={props.form}
          hostNames={props.hostNames}
          suffixfilesList={props.suffixfilesList}
          filePathList={props.filePathList}
          slicingRuleLogList={props.slicingRuleLogList}
          setLogListFile={setLogListFile}
          logListFile={logListFile}
          isNotLogPath={props.isNotLogPath}
          setisNotLogPath={props.setisNotLogPath}
        />
        <LogFileType
          form={props.form}
          setLogListFile={setLogListFile}
          logListFile={logListFile}
          isNotLogPath={props.isNotLogPath}
          setisNotLogPath={props.setisNotLogPath}
        />
        <LogRepeatForm
          logType='file'
          getKey={0}
          form={props.form}
          slicingRuleLog={props.slicingRuleLog}
        />
        {/* <Collapse
          bordered={false}
          expandIconPosition="right"
          onChange={collapseCallBack}
          activeKey={activeKeys?.length ? ['high'] : []}
          destroyInactivePanel
          style={{ background: 'none', padding: '10px 0 0' }}
        >
          <Panel
            header={<h2 style={{ display: 'inline-block', color: '#a2a2a5', marginLeft: '15px' }}>????????????</h2>}
            extra={<a>{activeKeys?.length ? <>??????&nbsp;<UpOutlined /></> : <>??????&nbsp;<DownOutlined /></>}</a>}
            showArrow={false}
            key="high"
            style={customPanelStyle}
          >
            <Form.Item label="??????????????????">
              {getFieldDecorator('step2_needLogContentFilter', {
                initialValue: 0,
                rules: [{ required: true, message: '?????????????????????????????????' }],
              })(
                <Radio.Group onChange={onLogFilterChange}>
                  <Radio value={0}>???</Radio>
                  <Radio value={1}>???</Radio>
                </Radio.Group>
              )}
            </Form.Item>

            {logFilter === 1 && <>
              <Row className="form-row">
                <Form.Item label="????????????">
                  <Col span={6}>
                    {getFieldDecorator('step2_logContentFilterType', {
                      initialValue: 0,
                      rules: [{ required: true, message: '?????????????????????' }],
                    })(
                      <Radio.Group>
                        <Radio value={0}>??????</Radio>
                        <Radio value={1}>?????????</Radio>
                      </Radio.Group>
                    )}
                  </Col>
                  <Col span={8}>
                    <Form.Item>
                      {getFieldDecorator('step2_logContentFilterExpression', {
                        initialValue: '',
                        rules: [{ required: true, message: `?????????${getFieldValue('step2_logContentFilterType') === 0 ? '??????' : '?????????'}???????????????` }],
                      })(<Input placeholder='??????&&???||?????????str1&&str2||str3' className='w-300' />)}
                    </Form.Item>
                  </Col>
                </Form.Item>
              </Row>
            </>}
            <Form.Item label="????????????????????????" className='col-unit-log'>
              <Row>
                <Col span={7}>
                  {getFieldDecorator(`step2_maxBytesPerLogEvent`, {
                    initialValue: 2,
                    rules: [{ required: true, message: '?????????????????????????????????' }],
                  })(
                    <InputNumber className='w-200' min={1} placeholder='???????????????' />,
                  )}
                </Col>
                <Col span={5}>
                  <Form.Item>
                    {getFieldDecorator(`step2_flowunit`, {
                      initialValue: flowUnitList[1]?.value,
                      rules: [{ required: true, message: '?????????' }],
                    })(
                      <Select className='w-100'>
                        {flowUnitList.map((v: ILabelValue, index: number) => (
                          <Select.Option key={index} value={v.value}>
                            {v.label}
                          </Select.Option>
                        ))}
                      </Select>,
                    )}
                  </Form.Item>
                </Col>
              </Row>
            </Form.Item>
          </Panel>
        </Collapse> */}
        {/* ????????? file */}
        {/* {collectLogType === 'file' ?
          <LoopAddLogFileType
            form={props.form}
            suffixfilesList={props.suffixfilesList}
            filePathList={props.filePathList}
            slicingRuleLogList={props.slicingRuleLogList}
          />
          : <> 
            <CatalogPathList form={props.form} cataPathlist={props.cataPathlist} collectLogType={collectLogType} />

            <Form.Item label="????????????" extra='??????????????????????????????????????????1??????????????????????????????'>
              {getFieldDecorator('step2_catalog_directoryCollectDepth', {
                initialValue: '1',
                rules: [{ required: true, message: '??????????????????????????????' }],
              })(<Input placeholder='??????????????????????????????' />)}
            </Form.Item>

            <Form.Item label="?????????????????????">
              {getFieldDecorator(`step2_catalog_collectwhitelist`, {
                initialValue: '',
                rules: [{ required: false }],
              })(<Input className='w-300' placeholder='????????????????????????' />)}
            </Form.Item>

            <Form.Item label="?????????????????????">
              {getFieldDecorator(`step2_catalog_collectblacklist`, {
                initialValue: '',
                rules: [{ required: false }],
              })(<Input className='w-300' placeholder='????????????????????????' />)}
            </Form.Item>

            <LogRepeatForm
              logType='catalog'
              getKey={''}
              form={props.form}
              slicingRuleLog={props.slicingRuleLog}
            />
          </>} */}

      </Form>
    </div>
  );
};

export default connect(null, mapDispatchToProps)(CollectLogConfiguration);
