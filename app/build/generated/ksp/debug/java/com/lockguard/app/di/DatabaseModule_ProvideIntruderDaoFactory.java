package com.lockguard.app.di;

import com.lockguard.app.data.database.IntruderDao;
import com.lockguard.app.data.database.LockGuardDatabase;
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
public final class DatabaseModule_ProvideIntruderDaoFactory implements Factory<IntruderDao> {
  private final Provider<LockGuardDatabase> databaseProvider;

  public DatabaseModule_ProvideIntruderDaoFactory(Provider<LockGuardDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public IntruderDao get() {
    return provideIntruderDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideIntruderDaoFactory create(
      Provider<LockGuardDatabase> databaseProvider) {
    return new DatabaseModule_ProvideIntruderDaoFactory(databaseProvider);
  }

  public static IntruderDao provideIntruderDao(LockGuardDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideIntruderDao(database));
  }
}
