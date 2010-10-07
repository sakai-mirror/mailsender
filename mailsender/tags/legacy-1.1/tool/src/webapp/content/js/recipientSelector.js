var RcptSelect = function()
{
	// holds LinkResult objects
	var cachedResults = new Array();
	var lastLink = null;
	var lastLinkReplacement = null;

	/**
	 * LinkResults object to hold data
	 *
	 * link: the link to follow (AJAX)
	 * content: the content returned after following link
	 *   showing/hiding results.  Expected values are >= 1.
	 */
	function LinkResult(link, content, resultFieldId)
	{
		this.link = link;
		this.content = content;
		this.resultFieldId = resultFieldId;
	}

	/**
	 * Get the results of of the given link from temporary cache.
	 */
	function getLinkResultCache(link)
	{
		var result = null;
		for (i in cachedResults)
		{
			if (cachedResults[i].link == link)
			{
				result = cachedResults[i];
				break;
			}
		}
		return result;
	}

	return {
		/**
		 * Show the results obtained from an ajax call.  If the call was made previously, the
		 * previous are returned from the cache.
		 *
		 * link: the link to follow for data retrieval.  should be an anchor object
		 * resultFieldId: the id of the field where results should be placed after retrieval
		 * event: the event generated from clicking the link
		 * isTLM: flags this link as a top level menu
		 */
		showResults : function(link, resultFieldId, event, isTLM)
		{
			// throw out the wait sign
			jQuery('#waitingDiv').show();

			var resultField = document.getElementById(resultFieldId);

			// check the cache for previous lookups
			var cachedResult = getLinkResultCache(link);
	
			// if no cached content found, request content from server
			if (cachedResult == null)
			{
				// this function (callback) defines what to do when the ajax response is received,
				// response will be placed in the "results" variable
				var callback = function(results)
				{
					// add the link to the queried array
					linkItem = new LinkResult(link, results, resultFieldId);
					cachedResults.push(linkItem);
	
					// put the results on the page
					resultField.innerHTML = results;
				}

				// setup the function that initiates the AJAX request
				var updater = RSF.getAJAXLinkUpdater(link, callback);

				// update the page
				updater();

				// show the results
				jQuery('#resultFieldId').show();
			}
			else
			{
				// if the element is visible, it hasn't been hidden by some other action so just
				// show whatever was left in the element rather than getting it from cache
//				if (MailtoolUtil.isVisible(resultFieldId))
//				{
					// set the content to the page
					resultField.innerHTML = cachedResult.content;
//				}
				//resultField.parentNode.replaceChild(cachedResult.content, resultField);
				jQuery('#resultFieldId').show();

				// update the fields of the result record
				cachedResult.resultFieldId = resultFieldId;
			}

			// create a text version of the link
			var linkText = link.innerHTML;
			var linkTextNode = document.createTextNode(linkText);

			// set the last link to the current clicked link
			if (isTLM)
			{
				// make the last link a clickable link instead of text
				if (lastLink && lastLinkReplacement)
				{
					lastLinkReplacement.parentNode.replaceChild(lastLink, lastLinkReplacement);
				}
				lastLink = link;
				lastLinkReplacement = linkTextNode;

				// replace the link with just the text of the link
				link.parentNode.replaceChild(linkTextNode, link);
			}

			// take down the wait sign
			jQuery('#waitingDiv').hide();
		}
	}; // end return
}(); // end namespace