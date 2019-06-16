package com.codingchili.mouse.enigma.autofill

import android.R
import android.app.assist.AssistStructure
import android.os.CancellationSignal
import android.os.Parcel
import android.service.autofill.*
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews



/**
 * Autofill service: work in progress.
 */
class AutoService: AutofillService() {

    override fun onFillRequest(
            request: FillRequest,
            cancellationSignal: CancellationSignal,
            callback: FillCallback
    ) {
        Log.w("autofill", "DING DING FILLREQUEST")

        // Get the structure from the request
        val contexts: List<FillContext> = request.fillContexts
        val structure: AssistStructure = contexts[contexts.size - 1].structure

        contexts.forEach { context ->
            traverseStructure(context.structure)
        }

        // Traverse the structure looking for nodes to fill out.
        val parsedStructure: ParsedStructure = parseStructure(structure)

        // Fetch user data that matches the fields.
        val (username: String, password: String) = fetchUserData(parsedStructure)

        // Build the presentation of the datasets
        val usernamePresentation = RemoteViews(packageName, R.layout.simple_list_item_1)
        usernamePresentation.setTextViewText(android.R.id.text1, username)

        val passwordPresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
        passwordPresentation.setTextViewText(android.R.id.text1, password)

        // Add a dataset to the response
        val fillResponse: FillResponse = FillResponse.Builder()
                .addDataset(Dataset.Builder()
                        .setValue(
                                parsedStructure.usernameId,
                                AutofillValue.forText(username),
                                usernamePresentation
                        )
                        .setValue(
                                parsedStructure.passwordId,
                                AutofillValue.forText(password),
                                passwordPresentation
                        )
                        .build())
                .build()

        // If there are no errors, call onSuccess() and pass the response
        callback.onSuccess(fillResponse)
    }

    fun traverseStructure(structure: AssistStructure) {
        val windowNodes: List<AssistStructure.WindowNode> =
                structure.run {
                    (0 until windowNodeCount).map { getWindowNodeAt(it) }
                }

        windowNodes.forEach { windowNode: AssistStructure.WindowNode ->
            val viewNode: AssistStructure.ViewNode? = windowNode.rootViewNode
            traverseNode(viewNode)
        }
    }

    fun traverseNode(viewNode: AssistStructure.ViewNode?) {
        if (viewNode?.autofillHints?.isNotEmpty() == true) {
            Log.w("autofill", viewNode.autofillHints?.joinToString(separator = ", "))
            // If the client app provides autofill hints, you can obtain them using:
            // viewNode.getAutofillHints();
        } else {
            Log.w("autofill", "${viewNode?.hint} - ${viewNode?.text?.toString()} - ${viewNode?.webDomain} - ${viewNode?.autofillType} - " +
                    "${viewNode?.autofillId} - ${viewNode?.autofillValue} - ${viewNode?.autofillOptions?.joinToString(separator = "")} - " +
                    "${viewNode?.className} - ${viewNode?.idType} - ${viewNode?.htmlInfo?.tag} - ${viewNode?.htmlInfo?.attributes?.joinToString(separator = ",")}")
            // Or use your own heuristics to describe the contents of a view
            // using methods such as getText() or getHint().


        }

        val children: List<AssistStructure.ViewNode>? =
                viewNode?.run {
                    (0 until childCount).map { getChildAt(it) }
                }

        children?.forEach { childNode: AssistStructure.ViewNode ->
            traverseNode(childNode)
        }
    }

    private fun fetchUserData(parsedStructure: ParsedStructure): UserData {
        return UserData("robin", "testing")
    }

    fun parseStructure(structure: AssistStructure): ParsedStructure {
        return ParsedStructure(
                AutofillId.CREATOR.createFromParcel(Parcel.obtain()),
                AutofillId.CREATOR.createFromParcel(Parcel.obtain())
        )
    }

    private fun parseWebDomain(viewNode: AssistStructure.ViewNode, validWebDomain: StringBuilder) {
        val webDomain = viewNode.webDomain
        if (webDomain != null) {
            Log.w("child web domain: %s", webDomain)
            if (validWebDomain.length > 0) {
                if (webDomain != validWebDomain.toString()) {
                    throw SecurityException("Found multiple web domains: valid= "
                            + validWebDomain + ", child=" + webDomain)
                }
            } else {
                validWebDomain.append(webDomain)
            }
        }
    }

    private fun parseNode(root: AssistStructure.ViewNode, allHints: MutableList<String>,
                          autofillSaveType: Int, autofillIds: MutableList<AutofillId>,
                          focusedAutofillIds: MutableList<AutofillId>) {
       /* val hints = root.autofillHints
        if (hints != null) {
            for (hint in hints) {
                val fieldTypeWithHints = mFieldTypesByAutofillHint.get(hint)
                if (fieldTypeWithHints != null && fieldTypeWithHints!!.fieldType != null) {
                    allHints.add(hint)
                    autofillSaveType.value = autofillSaveType.value or fieldTypeWithHints!!.fieldType.getSaveInfo()
                    autofillIds.add(root.autofillId!!)
                }
            }
        }
        if (root.isFocused) {
            focusedAutofillIds.add(root.autofillId!!)
        }*/
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        callback.onSuccess()
    }

    data class ParsedStructure(var usernameId: AutofillId, var passwordId: AutofillId)

    data class UserData(var username: String, var password: String)

}