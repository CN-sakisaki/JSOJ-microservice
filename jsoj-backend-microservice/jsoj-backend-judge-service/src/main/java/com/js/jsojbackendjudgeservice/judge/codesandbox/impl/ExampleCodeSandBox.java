package com.js.jsojbackendjudgeservice.judge.codesandbox.impl;

import cn.hutool.core.io.FileUtil;
import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.exception.BusinessException;
import com.js.jsojbackendcommon.exception.ThrowUtils;
import com.js.jsojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import com.js.jsojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.js.jsojbackendmodel.codesandbox.ExecuteCodeResponse;
import com.js.jsojbackendmodel.codesandbox.JudgeInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * 原生实现代码沙箱，用于安全地执行用户提交的代码
 * @author sakisaki
 * @date 2025/3/9 20:08
 */
@Slf4j
public class ExampleCodeSandBox implements CodeSandBox {

    // 代码存储的目录
    private static final String CODE_DIR = "resource/code/";
    // 代码执行的超时时间，单位为毫秒
    private static final long TIME_OUT = 5000L;
    // 安全管理器类名
    private static final String SECURITY_MANAGER_CLASS = "MySecurityManager";
    // 安全策略文件的路径
    private static final String SECURITY_POLICY_PATH = "resource/security.policy";

    /**
     * 执行代码的主要方法，包含创建临时目录、保存代码文件、编译代码和执行程序等步骤
     * @param executeCodeRequest 执行代码的请求对象，包含输入用例、代码和编程语言等信息
     * @return 执行代码的响应对象，包含输出用例、执行信息、执行状态和判题信息等
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        // 从请求对象中获取代码、输入用例和编程语言
        String code = executeCodeRequest.getCode();
        List<String> inputList = executeCodeRequest.getInputList();
        String language = executeCodeRequest.getLanguage();

        // 1. 创建临时目录，使用 UUID 保证目录名的唯一性
        String dirName = UUID.randomUUID().toString();
        String codeDirPath = CODE_DIR + dirName;
        File codeDir = new File(codeDirPath);
        // 若创建目录失败，抛出业务异常
        ThrowUtils.throwIf(!codeDir.mkdirs(), ErrorCode.OPERATION_ERROR, "创建代码目录失败");

        try {
            // 2. 保存代码文件
            File codeFile = saveCodeFile(codeDir, code);

            // 3. 编译代码
            ExecuteCodeResponse compileResponse = compileCode(codeDir, codeFile);
            // 若编译过程出现问题，返回编译响应
            if (compileResponse != null) return compileResponse;

            // 4. 执行程序
            return runProgram(codeDir, inputList);
        } catch (Exception e) {
            // 若执行过程中出现异常，抛出系统错误异常
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            // 清理临时目录
            if (codeDir.exists()) {
                boolean delete = FileUtil.del(codeDir);
                if (!delete) log.error("删除临时目录失败: {}", codeDirPath);
            }
        }
    }

    /**
     * 将代码保存到指定目录下的文件中
     * @param codeDir 代码存储的目录
     * @param code 要保存的代码
     * @return 保存代码的文件对象
     * @throws IOException 保存文件时可能出现的 IO 异常
     */
    private File saveCodeFile(File codeDir, String code) throws IOException {
        // 创建一个名为 Main.java 的文件
        File codeFile = new File(codeDir, "Main.java");
        // 将代码以 UTF-8 编码写入文件
        Files.write(codeFile.toPath(), code.getBytes(StandardCharsets.UTF_8));
        return codeFile;
    }

    /**
     * 编译指定目录下的代码文件
     * @param codeDir 代码存储的目录
     * @param codeFile 要编译的代码文件
     * @return 编译代码的响应对象，若编译成功返回 null
     * @throws IOException 编译过程中可能出现的 IO 异常
     * @throws InterruptedException 编译过程中可能出现的中断异常
     */
    private ExecuteCodeResponse compileCode(File codeDir, File codeFile)
            throws IOException, InterruptedException {

        // 创建一个进程来执行编译命令
        Process compileProcess = new ProcessBuilder(
                "javac",
                "-J-Djava.security.manager",
                "-J-Djava.security.policy=" + SECURITY_POLICY_PATH,
                codeFile.getName()
        ).directory(codeDir).start();

        // 读取编译错误信息
        String compileError = readStream(compileProcess.getErrorStream());
        // 等待编译进程结束并获取退出码
        int compileExitCode = compileProcess.waitFor();
        // 若编译失败，抛出业务异常
        ThrowUtils.throwIf(compileExitCode != 0,
                ErrorCode.SYSTEM_ERROR, "编译错误: " + compileError);
        return null;
    }

    /**
     * 运行编译后的程序
     * @param codeDir 代码存储的目录
     * @param inputList 输入用例列表
     * @return 执行程序的响应对象，包含输出用例、执行信息、执行状态和判题信息等
     * @throws IOException 执行过程中可能出现的 IO 异常
     * @throws InterruptedException 执行过程中可能出现的中断异常
     * @throws ExecutionException 异步读取流时可能出现的执行异常
     * @throws TimeoutException 执行超时可能出现的异常
     */
    private ExecuteCodeResponse runProgram(File codeDir, List<String> inputList) throws IOException, InterruptedException, ExecutionException, TimeoutException {

        // 将输入用例列表转换为字符串，以换行符分隔
        String inputStr = String.join("\n", inputList);
        // 创建一个进程来执行程序
        Process runProcess = new ProcessBuilder(
                "java",
                "-Djava.security.manager",
                "-Djava.security.policy=" + SECURITY_POLICY_PATH,
                "Main"
        ).directory(codeDir).start();

        // 将输入数据写入程序的标准输入流
        try (OutputStream stdIn = runProcess.getOutputStream()) {
            stdIn.write(inputStr.getBytes(StandardCharsets.UTF_8));
            stdIn.flush();
        }

        // 异步读取程序的标准输出流和错误输出流
        Future<String> outputFuture = readAsync(runProcess.getInputStream());
        Future<String> errorFuture = readAsync(runProcess.getErrorStream());

        // 等待程序执行，若超时则终止程序
        boolean finished = runProcess.waitFor(TIME_OUT, TimeUnit.MILLISECONDS);
        if (!finished) {
            runProcess.destroy();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "执行超时");
        }

        // 获取程序的输出和错误信息
        String output = outputFuture.get(500, TimeUnit.MILLISECONDS);
        String error = errorFuture.get(500, TimeUnit.MILLISECONDS);

        // 获取程序的退出码
        int exitCode = runProcess.exitValue();
        // 若程序执行失败，抛出业务异常
        ThrowUtils.throwIf(exitCode != 0,
                ErrorCode.OPERATION_ERROR, "运行错误: " + error);

        // 将程序的输出按换行符分割为列表
        List<String> outputList = Arrays.asList(output.split("\n"));
        // 检查输出数量是否与输入数量匹配，若不匹配则抛出业务异常
        ThrowUtils.throwIf(outputList.size() != inputList.size(),
                ErrorCode.OPERATION_ERROR, "输出与输入数量不匹配");

        // 创建判题信息对象
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage("执行成功");
        judgeInfo.setTime(System.currentTimeMillis());
        // 构建执行代码的响应对象
        return ExecuteCodeResponse.builder()
                .outputList(outputList)
                .judgeInfo(judgeInfo)
                .build();
    }

    /**
     * 从输入流中读取数据并转换为字符串
     * @param inputStream 输入流
     * @return 读取到的字符串
     * @throws IOException 读取流时可能出现的 IO 异常
     */
    private String readStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        }
    }

    /**
     * 异步读取输入流中的数据
     * @param inputStream 输入流
     * @return 包含读取结果的 Future 对象
     */
    private Future<String> readAsync(InputStream inputStream) {
        // 创建一个单线程的线程池
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // 提交一个任务到线程池，异步读取输入流
        return executor.submit(() -> readStream(inputStream));
    }
}