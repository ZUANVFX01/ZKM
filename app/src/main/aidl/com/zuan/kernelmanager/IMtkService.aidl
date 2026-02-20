// File: src/main/aidl/com/zuan/kernelmanager/IMtkService.aidl
package com.zuan.kernelmanager;

interface IMtkService {
    String readNode(String path);
    boolean writeNode(String path, String value);
    boolean nodeExists(String path);
    List<String> listDirectories(String path);
}