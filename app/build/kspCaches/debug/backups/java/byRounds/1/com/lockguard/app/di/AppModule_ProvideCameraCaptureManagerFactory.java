package com.lockguard.app.di;

import android.content.Context;
import com.lockguard.app.data.camera.CameraCaptureManager;
import com.lockguard.app.data.security.CryptoManager;
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
public final class AppModule_ProvideCameraCaptureManagerFactory implements Factory<CameraCaptureManager> {
  private final Provider<Context> contextProvider;

  private final Provider<CryptoManager> cryptoManagerProvider;

  public AppModule_ProvideCameraCaptureManagerFactory(Provider<Context> contextProvider,
      Provider<CryptoManager> cryptoManagerProvider) {
    this.contextProvider = contextProvider;
    this.cryptoManagerProvider = cryptoManagerProvider;
  }

  @Override
  public CameraCaptureManager get() {
    return provideCameraCaptureManager(contextProvider.get(), cryptoManagerProvider.get());
  }

  public static AppModule_ProvideCameraCaptureManagerFactory create(
      Provider<Context> contextProvider, Provider<CryptoManager> cryptoManagerProvider) {
    return new AppModule_ProvideCameraCaptureManagerFactory(contextProvider, cryptoManagerProvider);
  }

  public static CameraCaptureManager provideCameraCaptureManager(Context context,
      CryptoManager cryptoManager) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideCameraCaptureManager(context, cryptoManager));
  }
}
