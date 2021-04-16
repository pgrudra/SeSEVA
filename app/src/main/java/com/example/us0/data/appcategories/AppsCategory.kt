package com.example.us0.data.appcategories

import com.example.us0.AppsCategoryType
import com.example.us0.CategoryRuleStatus

data class AppsCategory(
    val categoryName:String,
    val categoryType: AppsCategoryType,
    var ruleBroken:CategoryRuleStatus,
    var timeLimit:Int?=null,
    var appLaunchLimit:Int?=null,
    var penalty:Int?=null
)