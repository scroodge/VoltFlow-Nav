package com.bridge.yandexbyd;

interface IShizukuSetupService {
    boolean grantWriteSecureSettings(String packageName);
    boolean enableAccessibility(String packageName, String serviceComponent);
    boolean allowProjectMedia(String packageName);
}
