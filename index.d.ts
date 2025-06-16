declare module 'openinstall-react-native-global' {

  export interface OpResult {
    channel: string;
    data: any;
    shouldRetry: boolean;
    message: string;
  }

  export default class openinstall {
    static init(options?: Record<string, any>): void;
    
    static getInstall(timeout: number, callback: (result: OpResult) => void): void;
    
    static addWakeUpListener(callback: (result: OpResult) => void): void;
    
    static removeWakeUpListener(callback: (result: OpResult) => void): void;
    
    static reportRegister(): void;
    
    static reportEffectPoint(effectID: string, effectValue: number, extraMap?: Record<string, any>): void;
    
    static reportShare(shareCode: string, sharePlatform: string, callback: (result: OpResult) => void): void;
  }
}