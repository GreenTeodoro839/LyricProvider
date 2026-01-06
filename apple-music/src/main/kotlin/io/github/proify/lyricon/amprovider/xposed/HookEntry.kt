package io.github.proify.lyricon.amprovider.xposed

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import io.github.proify.lyricon.amprovider.xposed.apple.Apple

@InjectYukiHookWithXposed(modulePackageName = Constants.APP_PACKAGE_NAME)
open class HookEntry : IYukiHookXposedInit {

    override fun onHook() = YukiHookAPI.encase {
        loadApp(Constants.APPLE_MUSIC_PACKAGE_NAME, Apple)
    }

    override fun onInit() {
        super.onInit()
        YukiHookAPI.configs {
            debugLog {
                tag = "AMProvider"
                isEnable = true
                elements(TAG, PRIORITY, PACKAGE_NAME, USER_ID)
            }
        }
    }

}