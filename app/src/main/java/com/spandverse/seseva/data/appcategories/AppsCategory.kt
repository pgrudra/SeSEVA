package com.spandverse.seseva.data.appcategories

import com.spandverse.seseva.AppsCategoryType
import com.spandverse.seseva.CategoryRuleStatus

data class AppsCategory(
    val categoryName:String,
    val categoryType: AppsCategoryType,
    var ruleBroken:CategoryRuleStatus,
    var timeLimit:Int?=null,
    var appLaunchLimit:Int?=null,
    var penalty:Int?=null
)