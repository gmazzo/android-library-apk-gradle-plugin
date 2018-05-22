package gs.apklib.example.module

import dagger.Binds
import dagger.Module
import dagger.Reusable

@Module
abstract class EntryPointModule {

    @Binds
    @Reusable
    internal abstract fun bindMyService(impl: MyServiceImpl): MyService

}
