package com.js.jsojbackendjudgeservice.judge.codesandbox;


import com.js.jsojbackendjudgeservice.judge.codesandbox.impl.ExampleCodeSandBox;
import com.js.jsojbackendjudgeservice.judge.codesandbox.impl.RemoteCodeSandBox;
import com.js.jsojbackendjudgeservice.judge.codesandbox.impl.ThirdPartyCodeSandBox;

/**
 * 代码沙箱工厂（根据字符串参数创建指定的代码沙箱实例），
 * @author JianShang
 * @version 1.0.0
 * @time 2024-10-24 02:00:36
 */
public class CodeSandBoxFactory {

    /**
     * 创建代码沙箱实例
     *
     * @param type 沙箱类型
     * @return
     */
    public static CodeSandBox newInstance(String type) {
        switch (type) {
            case "remote":
                return new RemoteCodeSandBox();
            case "thirdParty":
                return new ThirdPartyCodeSandBox();
            case "example":
            default:
                return new ExampleCodeSandBox();
        }
    }
}
