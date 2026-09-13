package com.lockguard.app.data.repository;

import android.content.Context;
import com.lockguard.app.data.camera.CameraCaptureManager;
import com.lockguard.app.data.database.IntruderDao;
import com.lockguard.app.data.security.BiometricHelper;
import com.lockguard.app.data.security.PinManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class SettingsRepositoryImpl_Factory implements Factory<SettingsRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<PinManager> pinManagerProvider;

  private final Provider<BiometricHelper> biometricHelperProvider;

  private final Provider<CameraCaptureManager> cameraCaptureManagerProvider;

  private final Provider<IntruderDao> intruderDaoProvider;

  public SettingsRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<PinManager> pinManagerProvider, Provider<BiometricHelper> biometricHelperProvider,
      Provider<CameraCaptureManager> cameraCaptureManagerProvider,
      Provider<IntruderDao> intruderDaoProvider) {
    this.contextProvider = contextProvider;
    this.pinManagerProvider = pinManagerProvider;
    this.biometricHelperProvider = biometricHelperProvider;
    this.cameraCaptureManagerProvider = cameraCaptureManagerProvider;
    this.intruderDaoProvider = intruderDaoProvider;
  }

  @Override
  public SettingsRepositoryImpl get() {
    return newInstance(contextProvider.get(), pinManagerProvider.get(), biometricHelperProvider.get(), cameraCaptureManagerProvider.get(), intruderDaoProvider.get());
  }

  public static SettingsRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<PinManager> pinManagerProvider, Provider<BiometricHelper> biometricHelperProvider,
      Provider<CameraCaptureManager> cameraCaptureManagerProvider,
      Provider<IntruderDao> intruderDaoProvider) {
    return new SettingsRepositoryImpl_Factory(contextProvider, pinManagerProvider, biometricHelperProvider, cameraCaptureManagerProvider, intruderDaoProvider);
  }

  public static SettingsRepositoryImpl newInstance(Context context, PinManager pinManager,
      BiometricHelper biometricHelper, CameraCaptureManager cameraCaptureManager,
      IntruderDao intruderDao) {
    return new SettingsRepositoryImpl(context, pinManager, biometricHelper, cameraCaptureManager, intruderDao);
  }
}
