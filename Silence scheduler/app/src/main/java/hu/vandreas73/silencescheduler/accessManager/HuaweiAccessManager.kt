package hu.vandreas73.silencescheduler.accessManager

import android.content.Context
import android.content.Intent
import hu.vandreas73.silencescheduler.R

// Source: https://stackoverflow.com/questions/38995469/how-to-get-miui-security-app-auto-start-permission-programmatically/43108234#43108234
class HuaweiAccessManager(context: Context) : AccessManager(context){
    override fun getPreferenceName(): String {
        return "HuaweiProtectedApps"
    }

    override fun getIntent(): Intent {
        val intent = Intent()
        intent.setClassName(
            "com.huawei.systemmanager",
            "com.huawei.systemmanager.optimize.process.ProtectActivity"
        )
        return intent
    }

    override fun alertTitle(): String {
        return context.getString(R.string.huawei_access_title)
    }

    override fun alertMessage(): String {
       return context.getString(R.string.huawei_access_request_text)
    }

    override fun alertPositiveButtonText(): String {
        return context.getString(R.string.huawei_access_request_text)
    }

    override fun isManditorary(): Boolean {
        return false
    }

    //    private fun huaweiProtectedApps() {
//        try {
//            var cmd = "am start -n com.huawei.systemmanager/.optimize.process.ProtectActivity"
//            cmd += " --user " //+ getUserSerial()
//            Runtime.getRuntime().exec(cmd)
//        } catch (ignored: IOException) {
//        }
//    }

//    private fun getUserSerial(): String {
//        val userManager = getSystemService(android.content.Context.USER_SERVICE) ?: return ""
//        try {
//            val myUserHandleMethod: Method =
//                Process::class.java.getMethod("myUserHandle", *null as Array<Class<*>?>?)
//            val myUserHandle: Any =
//                myUserHandleMethod.invoke(Process::class.java, null as Array<Any?>?)
//            val getSerialNumberForUser: Method =
//                userManager.javaClass.getMethod("getSerialNumberForUser", myUserHandle.javaClass)
//            val userSerial = getSerialNumberForUser.invoke(userManager, myUserHandle) as Long
//            return userSerial?.toString() ?: ""
//        } catch (ignored: NoSuchMethodException) {
//        } catch (ignored: IllegalArgumentException) {
//        } catch (ignored: InvocationTargetException) {
//        } catch (ignored: IllegalAccessException) {
//        }
//        return ""
//    }
}