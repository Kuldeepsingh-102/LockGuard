package com.lockguard.app.data.repository;

import com.lockguard.app.data.camera.CameraCaptureManager;
import com.lockguard.app.data.database.IntruderDao;
import com.lockguard.app.data.security.CryptoManager;
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
public final class IntruderRepositoryImpl_Factory implements Factory<IntruderRepositoryImpl> {
  private final Provider<IntruderDao> intruderDaoProvider;

  private final Provider<CryptoManager> cryptoManagerProvider;

  private final Provider<CameraCaptureManager> cameraCaptureManagerProvider;

  public IntruderRepositoryImpl_Factory(Provider<IntruderDao> intruderDaoProvider,
      Provider<CryptoManager> cryptoManagerProvider,
      Provider<CameraCaptureManager> cameraCaptureManagerProvider) {
    this.intruderDaoProvider = intruderDaoProvider;
    this.cryptoManagerProvider = cryptoManagerProvider;
    this.cameraCaptureManagerProvider = cameraCaptureManagerProvider;
  }

  @Override
  public IntruderRepositoryImpl get() {
    return newInstance(intruderDaoProvider.get(), cryptoManagerProvider.get(), cameraCaptureManagerProvider.get());
  }

  public static IntruderRepositoryImpl_Factory create(Provider<IntruderDao> intruderDaoProvider,
      Provider<CryptoManager> cryptoManagerProvider,
      Provider<CameraCaptureManager> cameraCaptureManagerProvider) {
    return new IntruderRepositoryImpl_Factory(intruderDaoProvider, cryptoManagerProvider, cameraCaptureManagerProvider);
  }

  public static IntruderRepositoryImpl newInstance(IntruderDao intruderDao,
      CryptoManager cryptoManager, CameraCaptureManager cameraCaptureManager) {
    return new IntruderRepositoryImpl(intruderDao, cryptoManager, cameraCaptureManager);
  }
}
