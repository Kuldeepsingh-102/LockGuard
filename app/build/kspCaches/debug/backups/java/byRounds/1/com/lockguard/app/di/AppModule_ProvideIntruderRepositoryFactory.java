package com.lockguard.app.di;

import com.lockguard.app.data.camera.CameraCaptureManager;
import com.lockguard.app.data.database.IntruderDao;
import com.lockguard.app.data.repository.IntruderRepository;
import com.lockguard.app.data.security.CryptoManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideIntruderRepositoryFactory implements Factory<IntruderRepository> {
  private final Provider<IntruderDao> intruderDaoProvider;

  private final Provider<CryptoManager> cryptoManagerProvider;

  private final Provider<CameraCaptureManager> cameraCaptureManagerProvider;

  public AppModule_ProvideIntruderRepositoryFactory(Provider<IntruderDao> intruderDaoProvider,
      Provider<CryptoManager> cryptoManagerProvider,
      Provider<CameraCaptureManager> cameraCaptureManagerProvider) {
    this.intruderDaoProvider = intruderDaoProvider;
    this.cryptoManagerProvider = cryptoManagerProvider;
    this.cameraCaptureManagerProvider = cameraCaptureManagerProvider;
  }

  @Override
  public IntruderRepository get() {
    return provideIntruderRepository(intruderDaoProvider.get(), cryptoManagerProvider.get(), cameraCaptureManagerProvider.get());
  }

  public static AppModule_ProvideIntruderRepositoryFactory create(
      Provider<IntruderDao> intruderDaoProvider, Provider<CryptoManager> cryptoManagerProvider,
      Provider<CameraCaptureManager> cameraCaptureManagerProvider) {
    return new AppModule_ProvideIntruderRepositoryFactory(intruderDaoProvider, cryptoManagerProvider, cameraCaptureManagerProvider);
  }

  public static IntruderRepository provideIntruderRepository(IntruderDao intruderDao,
      CryptoManager cryptoManager, CameraCaptureManager cameraCaptureManager) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideIntruderRepository(intruderDao, cryptoManager, cameraCaptureManager));
  }
}
