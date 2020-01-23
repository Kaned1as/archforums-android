package com.kanedias.holywarsoo

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import butterknife.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kanedias.holywarsoo.model.MainPageModel
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.*

/**
 * Fragment responsible for adding account. Appears when you click "add account" in the sidebar.
 * This may be either registration or logging in.
 *
 * @author Kanedias
 *
 * Created on 2017-11-11
 */
class LoginFragment : Fragment() {

    @BindView(R.id.acc_username_input)
    lateinit var usernameInput: EditText

    @BindView(R.id.acc_password_input)
    lateinit var passwordInput: EditText

    private lateinit var progressDialog: Dialog

    private lateinit var mainPageModel: MainPageModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        ButterKnife.bind(this, view)

        mainPageModel = ViewModelProviders.of(requireActivity()).get(MainPageModel::class.java)

        progressDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.please_wait)
                .setMessage(R.string.logging_in)
                .create()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog.dismiss()
    }

    /**
     * Creates session for the user, saves auth and closes fragment on success.
     */
    @OnClick(R.id.confirm_button)
    fun doLogin() {
        lifecycleScope.launch {
            progressDialog.show()

            try {
                withContext(Dispatchers.IO) { Network.login(
                    username = usernameInput.text.toString(),
                    password = passwordInput.text.toString())
                }

                Toast.makeText(requireContext(), R.string.login_successful, Toast.LENGTH_SHORT).show()
                mainPageModel.account.value = Network.getUsername()
                fragmentManager?.popBackStack()

            } catch (ex: Exception) {
                Network.reportErrors(ctx = requireContext(), ex = ex)
            }

            progressDialog.hide()
        }
    }
}