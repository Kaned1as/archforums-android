package com.kanedias.holywarsoo

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.kanedias.holywarsoo.misc.resolveAttr
import com.marcoscg.easyabout.EasyAboutFragment
import com.marcoscg.easyabout.helpers.AboutItemBuilder
import com.marcoscg.easyabout.items.AboutCard
import com.marcoscg.easyabout.items.HeaderAboutItem
import com.marcoscg.easyabout.items.NormalAboutItem
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrConfig
import com.r0adkll.slidr.model.SlidrInterface
import com.r0adkll.slidr.model.SlidrPosition


/**
 * @author Kanedias
 *
 * Created on 05.03.19
 */
class AboutFragment: EasyAboutFragment() {

    private lateinit var donateHelper: DonateHelper

    override fun configureFragment(ctx: Context, root: View, state: Bundle?) {
        @Suppress("DEPRECATION")
        root.findViewById<View>(com.marcoscg.easyabout.R.id.main_view)
            .setBackgroundColor(root.resolveAttr(R.attr.colorSecondary))

        donateHelper = DonateHelper(ctx as AppCompatActivity)

        val appDescItem = HeaderAboutItem.Builder(ctx)
                .setTitle(R.string.app_name)
                .setSubtitle("By ${ctx.getString(R.string.the_maker)}")
                .setIcon(R.drawable.ic_launcher)
                .build()

        val versionItem = NormalAboutItem.Builder(context)
                .setTitle(R.string.version)
                .setSubtitle("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                .setIcon(R.drawable.information)

        val licenseItem = AboutItemBuilder.generateLinkItem(ctx, "https://www.gnu.org/licenses/gpl-3.0.ru.html")
                .setTitle(R.string.license)
                .setSubtitle(R.string.gplv3)
                .setIcon(R.drawable.description)

        val sourceCodeItem = AboutItemBuilder.generateLinkItem(ctx, "https://gitlab.com/Kanedias/holywarsoo-android")
                .setTitle(R.string.source_code)
                .setSubtitle(R.string.fork_on_gitlab)
                .setIcon(R.drawable.gitlab)

        val issueItem = AboutItemBuilder.generateLinkItem(ctx, "https://gitlab.com/Kanedias/holywarsoo-android/issues/new")
                .setTitle(R.string.report_bug)
                .setSubtitle(R.string.gitlab_issue_tracker)
                .setIcon(R.drawable.bug)

        val aboutAppCard = AboutCard.Builder(context)
                .addItem(appDescItem)
                .addItem(versionItem)
                .addItem(licenseItem)
                .addItem(sourceCodeItem)
                .addItem(issueItem)
                .build()

        val authorDescItem = AboutItemBuilder.generateLinkItem(ctx, "https://www.patreon.com/kanedias")
                .setTitle(R.string.the_maker)
                .setSubtitle(R.string.house_of_maker)
                .setIcon(R.drawable.feather)

        val supportDescItem = NormalAboutItem.Builder(ctx)
                .setTitle(R.string.donate)
                .setIcon(R.drawable.heart)
                .setOnClickListener { donateHelper.donate() }

        val emailDescItem = AboutItemBuilder.generateEmailItem(ctx, "kanedias@keemail.me")
                .setTitle(R.string.send_email)
                .setIcon(R.drawable.email)

        val aboutAuthorCard = AboutCard.Builder(context)
                .setTitle(R.string.author)
                .addItem(authorDescItem)
                .addItem(supportDescItem)
                .addItem(emailDescItem)
                .build()

        addCard(aboutAppCard)
        addCard(aboutAuthorCard)
    }

    /**
     * Slide right to go back helper
     */
    private var slidrInterface: SlidrInterface? = null

    override fun onResume() {
        super.onResume()
        if (slidrInterface == null) {
            val mainChild = (requireView() as ViewGroup).getChildAt(0)
            slidrInterface = Slidr.replace(mainChild, SlidrConfig.Builder().position(SlidrPosition.LEFT).build())
        }
    }
}