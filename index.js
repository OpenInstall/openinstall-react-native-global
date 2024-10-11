import {
  NativeModules,
  Platform,
  DeviceEventEmitter,
} from 'react-native';

const OpeninstallModule = NativeModules.OpeninstallModule;

const receiveWakeUpEvent = 'OpeninstallWakeupCallBack'
const listeners = {}

export default class openinstall {


  /**
   * 初始化
   */
  static init(options) {
    if (Platform.OS == 'android') {
      OpeninstallModule.init(options)
    } else {
      OpeninstallModule.initSDK(options)
    }
  }

  /**
   * 获取安装动态参数
   * @param {Int} s 超时时长,一般为10~15s 如果只是在后台默默统计或使用参数，可以设置更长时间
   * @param {Function} cb = (result)=> {} data和channel都为空时返回null
   */
  static getInstall(s, cb) {
    OpeninstallModule.getInstall(s, result => {
      cb(result)
    }
    )
  }


  /**
   * 监听univeral link或scheme拉起参数回调的方法
   * @param {Function} cb = (result)=> {} data和channel都为空时返回null
   */
  static addWakeUpListener(cb) {
    OpeninstallModule.getWakeUp(
      result => {
        cb(result)
      }
    )
    listeners[cb] = DeviceEventEmitter.addListener(
      receiveWakeUpEvent,
      result => {
        cb(result)
      }
    )

  }

  /**
   * 取消监听
   * @param {Function} cb = (Object) => { }
   */
  static removeWakeUpListener(cb) {
    if (!listeners[cb]) {
      return
    }
    listeners[cb].remove()
    listeners[cb] = null
  }

  /**
   * 上报注册事件
   */
  static reportRegister() {
    OpeninstallModule.reportRegister()
  }

  /**
   * 上报效果点
   * @param {string} 效果点ID
   * @param {number} 效果点值
   * @param {Object} 效果点明细自定义参数
   */
  static reportEffectPoint(effectID, effectValue, extraMap) {
    OpeninstallModule.reportEffectPoint(effectID, effectValue, extraMap)
  }

  /**
   * 
   * @param {string} 分享ID 
   * @param {string} 分享平台，参考官网平台列表
   * @param {Function} func  分享回调 (Object) => { }
   */
  static reportShare(shareCode, sharePlatform, func) {
    OpeninstallModule.reportShare(shareCode, sharePlatform, ret => {
      func(ret)
    })
  }

}

