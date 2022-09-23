package com.seekerslab.nvd.util;

/**
 * Application이 실행되는 OS 환경 검사
 * @file	OSSpecifier
 * @brief	OS 확인용 클래스
 * @version 1.0
 * @date	2018. 05. 02
 * @author	Seekers Inc. (lab@seekerslab.com)
 */
public class OSSpecifier {
	public boolean isWindows(String OS) {
        return (OS.indexOf("win") >= 0);
    }
    public boolean isMac(String OS) {
        return (OS.indexOf("mac") >= 0);
    }
    public boolean isUnix(String OS) {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
    }
    public boolean isSolaris(String OS) {
        return (OS.indexOf("sunos") >= 0);
    }
}
