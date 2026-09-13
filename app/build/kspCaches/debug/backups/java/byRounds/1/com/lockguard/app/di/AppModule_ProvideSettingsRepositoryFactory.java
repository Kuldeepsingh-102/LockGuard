package com.lockguard.app.di;

import android.content.Context;
import com.lockguard.app.data.camera.CameraCaptureManager;
import com.lockguard.app.data.database.IntruderDao;
import com.lockguard.app.data.repository.SettingsRepository;
import com.lockguard.app.data.security.BiometricHelper;
import com.lockguard.app.data.security.PinManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvideSettingsRepositoryFactory implements Factory<SettingsRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<PinManager> pinManagerProvider;

  private final Provider<BiometricHelper> biometricHelperProvider;

  private final Provider<CameraCaptureManager> cameraCaptureManagerProvider;

  private final Provider<IntruderDao> intruderDaoProvider;

  public AppModule_ProvideSettingsRepositoryFactory(Provider<Context> contextProvider,
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
  public SettingsRepository get() {
    return provideSettingsRepository(contextProvider.get(), pinManagerProvider.get(), biometricHelperProvider.get(), cameraCaptureManagerProvider.get(), intruderDaoProvider.get());
  }

  public static AppModule_ProvideSettingsRepositoryFactory create(Provider<Context> contextProvider,
      Provider<PinManager> pinManagerProvider, Provider<BiometricHelper> biometricHelperProvider,
      Provider<CameraCaptureManager> cameraCaptureManagerProvider,
      Provider<IntruderDao> intruderDaoProvider) {
    return new AppModule_ProvideSettingsRepositoryFactory(contextProvider, pinManagerProvider, biometricHelperProvider, cameraCaptureManagerProvider, intruderDaoProvider);
  }

  public static SettingsRepository provideSettingsRepository(Context context, PinManager pinManager,
      BiometricHelper biometricHelper, CameraCaptureManager cameraCaptureManager,
      IntruderDao intruderDao) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSettingsRepository(context, pinManager, biometricHelper, cameraCaptureManager, intruderDao));
  }
}
