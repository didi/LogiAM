import { message } from 'antd';
import moment from 'moment';
import { oneDayMillims } from '../constants/common';
import { ICookie, IStringMap } from '../interface/common';
import * as SparkMD5 from 'spark-md5';
import store from '../store';
import * as actions from '../actions';
import {useRef,useEffect,useCallback} from 'react';

export const getCookie = (key: string): string => {
  const map: IStringMap = {};
  document.cookie.split(';').map((kv) => {
    const d = kv.trim().split('=');
    map[d[0]] = d[1];
    return null;
  });
  return map[key];
};

export const setCookie = (cData: ICookie[]) => {
  const date = new Date();
  cData.forEach((ele: any) => {
    date.setTime(date.getTime() + (ele.time * oneDayMillims));
    const expires = 'expires=' + date.toUTCString();
    document.cookie = ele.key + '=' + ele.value + '; ' + expires + '; path=/';
  });
};

export const deleteCookie = (cData: string[]) => {
  setCookie(cData.map(i => ({ key: i, value: '', time: -1 })));
};

export const copyString = (url: any) => {
  const input = document.createElement('textarea');
  input.value = url;
  document.body.appendChild(input);
  input.select();
  if (document.execCommand('copy')) {
    message.success('ε€εΆζε');
  }
  input.remove();
};

export const getMoment = () => {
  return moment();
};

export const computeChecksumMd5 = (file: File) => {
  return new Promise((resolve, reject) => {
    const chunkSize = 2097152; // Read in chunks of 2MB
    const spark = new SparkMD5.ArrayBuffer();
    const fileReader = new FileReader();

    let cursor = 0; // current cursor in file

    fileReader.onerror = () => {
      reject('MD5 computation failed - error reading the file');
    };

    function processChunk(chunkStart: number) {
      const chunkEnd = Math.min(file.size, chunkStart + chunkSize);
      fileReader.readAsArrayBuffer(file.slice(chunkStart, chunkEnd));
    }

    fileReader.onload = (e: any) => {
      spark.append(e.target.result); // Accumulate chunk to md5 computation
      cursor += chunkSize; // Move past this chunk

      if (cursor < file.size) {
        processChunk(cursor);
      } else {
        // Computation ended, last chunk has been processed. Return as Promise value.
        // This returns the base64 encoded md5 hash, which is what
        // Rails ActiveStorage or cloud services expect
        // resolve(btoa(spark.end(true)));

        // If you prefer the hexdigest form (looking like
        // '7cf530335b8547945f1a48880bc421b2'), replace the above line with:
        // resolve(spark.end());
        resolve(spark.end());
      }
    };

    processChunk(0);
  });
};
export const judgeEmpty = (value: any) => {
  return (value === undefined || value === null || value === '') ? '' : value;
}

export const setLimitUnit = (value: any, v?: number) => {
  const val = Number(value);
  let maxBytesPerLogEvent = v || '';
  let flowunit = 1048576;
  if (val >= 1024) {
    maxBytesPerLogEvent = val / 1024;
    flowunit = 1024;
    if (val >= 1048576) {
      maxBytesPerLogEvent = val / 1024 / 1024;
      flowunit = 1048576;
    }
  }
  return {
    maxBytesPerLogEvent,
    flowunit,
  }
}

export const unique = (arr: any) => { // ε»ι
  return arr.filter((item: any, index: number, arr: any) => {
    //ε½εεη΄ οΌε¨εε§ζ°η»δΈ­ηη¬¬δΈδΈͺη΄’εΌ==ε½εη΄’εΌεΌοΌε¦εθΏεε½εεη΄ 
    return arr.indexOf(item, 0) === index;
  });
}

export const dealPostMessage = (data: any) => {
  const { type } = data;
  switch (type) {
    case 'permissionPoint':
      store.dispatch(actions.setPermissionPoints(data.value));
      break;
    // case 'tenantProject':
    //   store.dispatch(actions.setTenantProject(data.value));
    //   break;
  }
};


/**
   * @method useDebounce εΊη¨δΊhookηι²ζε½ζ°
   * @param {() => any} fn ιθ¦ε€ηηε½ζ°
   * @param {number} delay ε½ζ°εΊεηζΆι΄
   * @param dep δΌ ε₯η©Ίζ°η»οΌδΏθ―useCallbackζ°ΈθΏθΏεεδΈδΈͺε½ζ°
   */

  export const useDebounce = (fn:any, delay: number, dep = []) => {
    const { current } = useRef<any>({ fn, timer: null });
    useEffect(function () {
      current.fn = fn;
    }, [fn]);

    return useCallback(function f(...args) {
      if (current.timer) {
        clearTimeout(current.timer);
      }
      current.timer = setTimeout(() => {
        current.fn(...args);
      }, delay);
    }, dep)
  }


/**
 * @description: ε­θθ½¬ζ’εδ½
 * @param {*} limit: number ε­θ
 * @return {*} string θ½¬ζ’εηεδ½
 */
export function byteChange(limit: number){
  let size = "";
  if(limit < 1 * 1024){                            //ε°δΊ0.1KBοΌεθ½¬εζB
      size = limit.toFixed(2) + "B"
  }else if(limit < 1 * 1024 * 1024){            //ε°δΊ0.1MBοΌεθ½¬εζKB
      size = (limit/1024).toFixed(2) + "KB"
  }else if(limit < 1 * 1024 * 1024 * 1024){        //ε°δΊ0.1GBοΌεθ½¬εζMB
      size = (limit/(1024 * 1024)).toFixed(2) + "MB"
  }else{                                            //εΆδ»θ½¬εζGB
      size = (limit/(1024 * 1024 * 1024)).toFixed(2) + "GB"
  }

  let sizeStr = size + "";                        //θ½¬ζε­η¬¦δΈ²
  let index = sizeStr.indexOf(".");                    //θ·εε°ζ°ηΉε€ηη΄’εΌ
  let dou = sizeStr.substr(index + 1 ,2)            //θ·εε°ζ°ηΉεδΈ€δ½ηεΌ
  if(dou == "00"){                                //ε€ζ­εδΈ€δ½ζ―ε¦δΈΊ00οΌε¦ζζ―εε ι€00               
      return sizeStr.substring(0, index) + sizeStr.substr(index + 3, 2)
  }
  return size;
}

/**
 * @description: ε­θθ½¬ζ’ζMB
 * @param {number} limit
 * @return {*}
 */
export function byteToMB(limit: number) {
  let nums = limit / (1024 * 1024)
  if (nums === 0) {
    return 0;
  }
  if (nums < 0.01 && nums > 0) {
    return limit
  }
  return nums.toFixed(2);
}

export function nsTo(ns: number) {
  if (ns < 1000) {
    return ns + 'ns'
  }
  if (ns < 1000 * 10000) {
    return Number(ns / 1000).toFixed(2) + 'ΞΌs'
  }
  if (ns < 1000 * 1000 * 10000) {
    return timeStamp(Number((ns / 1000) / 1000));
  }
  return ns + 'ns'
}

export function ToMs(value: number) {
  let size = ((value / 1000) / 1000).toFixed(2);
  let sizeStr = size + "";                        //θ½¬ζε­η¬¦δΈ²
  let index = sizeStr.indexOf(".");                    //θ·εε°ζ°ηΉε€ηη΄’εΌ
  let dou = sizeStr.substr(index + 1 ,2)            //θ·εε°ζ°ηΉεδΈ€δ½ηεΌ
  if(dou == "00"){                                //ε€ζ­εδΈ€δ½ζ―ε¦δΈΊ00οΌε¦ζζ―εε ι€00               
      return sizeStr.substring(0, index) + sizeStr.substr(index + 3, 2)
  }
  return sizeStr;
}

export function Tous(value: number) {
    return Number((value / 1000)).toFixed(2);
}

/**
 * @description: msθ½¬ζδΈΊζε€§δΈΊ ζδ»½ηεδ½
 * @param {*} second_time
 * @return {*}
 */
export function timeStamp( mtime: number ){
  if (mtime < 1000) {
    return mtime.toFixed(2) + 'ms'
  }
  let second_time: any = (mtime / 1000);
  let time = (second_time).toFixed(2) + "s";
  if( parseInt(second_time)> 60){
  
    let second = parseInt(second_time) % 60;
    let min: any = parseInt(second_time / 60);
    time = min + "ε" + second + "η§";
    
    if( min > 60 ){
      min = parseInt(second_time / 60) % 60;
      let hour = parseInt( parseInt(second_time / 60) /60 );
      time = hour + "ε°ζΆ" + min + "ε" + second + "η§";
  
      if( hour > 24 ){
        hour = parseInt( parseInt(second_time / 60) /60 ) % 24;
        let day = parseInt( parseInt( parseInt(second_time / 60) /60 ) / 24 );
        time = day + "ε€©" + hour + "ε°ζΆ" + min + "ε" + second + "η§";
        if (day > 30) {
          day = parseInt( parseInt( parseInt(second_time / 60) /60 ) / 24) % 30;
          let m = parseInt( parseInt( parseInt(second_time / 60) /60 ) / 24 / 30 );
          time = m + "ζ" + day + "ε€©" + hour + "ε°ζΆ" + min + "ε" + second + "η§";
        }
      }
    }
    
  
  }
  
  return time;		
}

export function PercentageConversion (value: number, isTool: string): number | string {
  if (isTool) return value + '%'
  return value * 100
}

export function msecondToSecond (m: number) {
  const s = (m / 1000).toFixed(2) + '';
  const arr = s.split('.');
  if (arr[1] === '00') return arr[0];
  if (arr[1] === '50') return arr[0] + '.5';
  return s
} 

/**εζ°θ―΄ζοΌ 
 
* ζ Ήζ?ιΏεΊ¦ζͺεεδ½Ώη¨ε­η¬¦δΈ²οΌθΆιΏι¨εθΏ½ε β¦ 

* str ε―Ήθ±‘ε­η¬¦δΈ² 

* len η?ζ ε­θιΏεΊ¦ 

* θΏεεΌοΌ ε€ηη»ζε­η¬¦δΈ² 

*/

export function cutString(str: string, len: number) { 

  //lengthε±ζ§θ―»εΊζ₯ηζ±ε­ιΏεΊ¦δΈΊ1 

  if(str.length*2 <= len) { 

    return str; 

  } 

  var strlen = 0; 

  var s = ""; 

  for(var i = 0;i < str.length; i++) { 

    s = s + str.charAt(i); 

    if (str.charCodeAt(i) > 128) { 

      strlen = strlen + 2; 

      if(strlen >= len){ 

        return s.substring(0,s.length-1) + "..."; 

      } 

    } else { 

      strlen = strlen + 1; 

      if(strlen >= len){ 

        return s.substring(0,s.length-2) + "..."; 

      } 

    } 

  } 

  return s; 

} 


export function countChange(limit: number){
  let size = "";
  if(limit < 100 * 1000){                            //ε°δΊ100KB
      size = limit + ""
  }else if(limit < 100 * 1000 * 1000){            //ε°δΊ100MοΌεθ½¬εζK
      size = (limit/1000).toFixed(1) + "K"
  }else if(limit < 100 * 1000 * 1000 * 1000){        //ε°δΊ100GοΌεθ½¬εζMB
      size = (limit/(1000 * 1000)).toFixed(1) + "M"
  }

  let sizeStr = size + "";                        //θ½¬ζε­η¬¦δΈ²
  let index = sizeStr.indexOf(".");                    //θ·εε°ζ°ηΉε€ηη΄’εΌ
  let dou = sizeStr.substr(index + 1 ,2)            //θ·εε°ζ°ηΉεδΈ€δ½ηεΌ
  if(dou == "00"){                                //ε€ζ­εδΈ€δ½ζ―ε¦δΈΊ00οΌε¦ζζ―εε ι€00               
      return sizeStr.substring(0, index) + sizeStr.substr(index + 3, 2)
  }
  return sizeStr;
}