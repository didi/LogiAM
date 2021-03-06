import React, { useState, useEffect, useRef, useCallback } from 'react';
import { FormComponentProps } from 'antd/lib/form';
import { Form, Input, Radio, InputNumber, Button, AutoComplete, Select } from 'antd';
import LogRepeatForm from './LogRepeatForm';
import { regChar } from '../../constants/reg';
import { logFilePathKey } from './dateRegAndGvar';
import { getCollectPathList } from '../../api/collect';
import { useDebounce } from '../../lib/utils'
import './index.less';
import { getHostListbyServiceId } from '../../api';


interface ILogFileTypeProps extends FormComponentProps {
  getKey: number,
  suffixfiles: number;
  slicingRuleLog: number;
  addFileLog?: any;
}

const { TextArea } = Input;
const { Option } = Select;

const LogFileType = (props: any | ILogFileTypeProps) => {
  const { getFieldDecorator, getFieldValue, setFieldsValue } = props.form;
  const editUrl = window.location.pathname.includes('/edit-task');
  const [suffixfiles, setSuffixfiles] = useState(0);
  const [isNotLogPath, setIsNotLogPath] = useState(false);
  const [fileArrList, setFileArrList] = useState([])
  const [hostNameList, setHostNameList] = useState<any>([])
  // const initial = props?.addFileLog && !!Object.keys(props?.addFileLog)?.length;
  const options = hostNameList.length > 0 && hostNameList.map((group: any, index: number) => {
    return <Option key={group.id} value={group.hostName}>
      {group.hostName}
    </Option>
  })
  const onSuffixfilesChange = (e: any) => {
    setSuffixfiles(e.target.value);
  }

  const handleLogPath = useDebounce(() => {
    const serviceId = getFieldValue(`step1_serviceId`)
    const logSuffixfilesValue = getFieldValue(`step2_file_suffixMatchRegular`)
    const logFilePath = getFieldValue(`step2_file_path_${logFilePathKey}`)
    // const hostName = getFieldValue(`step2_hostName`)
    if (serviceId && logSuffixfilesValue && logFilePath) {
      getHostListbyServiceId(serviceId).then((res: any) => {
        if (res?.hostList?.length > 0) {
          props.setisNotLogPath(true)
          setHostNameList(res?.hostList)
          handlelogSuffixfiles()
        } else {
          props.setisNotLogPath(false)
          setHostNameList([])
        }
      })
    }
  }, 200)

  const handlelogSuffixfiles = useDebounce(() => {
    const logSuffixfilesValue = getFieldValue(`step2_file_suffixMatchRegular`)
    const logFilePath = getFieldValue(`step2_file_path_${logFilePathKey}`)
    const hostName = getFieldValue(`step2_hostName`)
    const params = {
      path: logFilePath,
      suffixMatchRegular: logSuffixfilesValue,
      hostName
    }
    if (logFilePath && logSuffixfilesValue && hostName) {
      getCollectPathList(params).then((res) => {
        // logArr[key] = res.message.split()
        setFileArrList(res)
      })
    }
  }, 0)
  const onLogFilterChange = (e: any) => {
    handlelogSuffixfiles()
  }

  useEffect(() => {
    if (editUrl) {
      setSuffixfiles(props.suffixfiles);
    }
  }, [props.suffixfiles]);

  useEffect(() => {
    setFileArrList(props.logListFile);
  }, [props.logListFile]);

  useEffect(() => {
    setIsNotLogPath(props.isNotLogPath)
  }, [props.isNotLogPath]);

  return (
    <div className='set-up' key={props.getKey}>
      {/* ????????? */}
      {/* <Form.Item label='???????????????'>
        {getFieldDecorator(`step2_file_suffixSeparationCharacter_${props.getKey}`, {
          initialValue: initial ? props?.addFileLog[`step2_file_suffixSeparationCharacter_${props.getKey}`] : '',
          rules: [{
            required: true,
            message: '??????????????????????????????._/????????????????????????',
            validator: (rule: any, value: string) => {
              return !!value && new RegExp(regChar).test(value);
            },
          }],
        })(
          <Input placeholder='??????????????????????????????._/????????????????????????' />,
        )}
      </Form.Item> */}

      {/* <Form.Item label='????????????????????????'>
        {getFieldDecorator(`step2_file_suffixMatchType_${props.getKey}`, {
          initialValue: initial ? props?.addFileLog[`step2_file_suffixMatchType_${props.getKey}`] : 1,
          rules: [{ required: true, message: '?????????????????????????????????' }],
        })(
          <Radio.Group onChange={onSuffixfilesChange}>
            <Radio value={0}>??????????????????</Radio>
            <Radio checked value={1}>????????????</Radio>
          </Radio.Group>,
        )}
      </Form.Item> */}

      {/* <Form.Item label="????????????" className={suffixfiles === 0 ? '' : 'hide'}>
        {getFieldDecorator(`step2_file_suffixLength_${props.getKey}`, {
          initialValue: initial ? props?.addFileLog[`step2_file_suffixLength_${props.getKey}`] : '',
          rules: [{ required: suffixfiles === 0, message: '?????????????????????' }],
        })(<InputNumber min={0} placeholder='?????????' />)}
      </Form.Item> */}
      {/* <Form.Item label="????????????" className={suffixfiles === 1 ? '' : 'hide'}> */}
      <Form.Item label="??????????????????????????????" extra='???:?????????????????????????????????,???????????????,?????????????????????????????????'>
        {getFieldDecorator(`step2_file_suffixMatchRegular`, {
          initialValue: '',
          rules: [{ required: true, message: '??????????????????????????????' }],
        })(<Input style={{ width: '400px' }} onChange={() => {
          setIsNotLogPath(false)
          setHostNameList([])
        }} placeholder='????????????????????????????????????????????????????????????^([\d]{0,6})$' />)}
        <Button onClick={handleLogPath} type="primary" style={{ marginLeft: '20px' }}>??????</Button>
      </Form.Item>
      {hostNameList.length > 0 && props.isNotLogPath ? <Form.Item label="????????????">
        {getFieldDecorator(`step2_hostName`, {
          initialValue: hostNameList[0].hostName,
          rules: [{ message: '???????????????????????????' }],
        })(
          <Select
            showSearch
            style={{ width: 200 }}
            placeholder="???????????????"
            optionFilterProp="children"
            onChange={handlelogSuffixfiles}
          >
            {options}
          </Select>
          // <Radio.Group onChange={onLogFilterChange}>
          //   {
          //     hostNameList?.map((ele: any, index: number) => {
          //       return <Radio key={ele.id} value={ele.id}>{ele.hostName}</Radio>
          //     })
          //   }
          // </Radio.Group>
        )}
      </Form.Item> : null}
      {
        hostNameList.length > 0 && props.isNotLogPath && <Form.Item>
          <ul className={`logfile_list logFileList`}>
            {
              fileArrList && fileArrList?.map((logfile: string, key: number) => <li key={key}>{logfile}</li>)
            }
          </ul>
        </Form.Item>
      }
      {/* ???????????? */}
    </div>
  );
};

export default LogFileType;
