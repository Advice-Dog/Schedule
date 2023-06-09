package com.advice.firebase.datasource

import com.advice.data.datasource.OrganizationsDataSource
import com.advice.data.datasource.TagsDataSource
import com.advice.data.datasource.VendorsDataSource
import kotlinx.coroutines.flow.combine

class FirebaseVendorsDataSource(
    private val organizationsDataSource: OrganizationsDataSource,
    private val tagsDataSource: TagsDataSource,
) : VendorsDataSource {

    override fun get() =
        combine(organizationsDataSource.get(), tagsDataSource.get()) { organizations, tags ->
            val vendor =
                tags.find { it.label == "Organization Type" }?.tags?.find { it.label == "Vendor" }
            organizations.filter { vendor == null || it.tags.contains(vendor.id) }
        }
}