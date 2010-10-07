var RcptSelect = function()
{
	// holds LinkResult objects
	var lastLink = null;
	var lastLinkReplacement = null;

	/**
	 * LinkResults object to hold data
	 *
	 * link: the link to follow (AJAX)
	 * content: the content returned after following link
	 *   showing/hiding results.  Expected values are >= 1.
	 */
	function LinkResult(link, content, resultSelector)
	{
		this.link = link;
		this.content = content;
		this.resultSelector = resultSelector;
	}

	function _safeId(type, id)
	{
	    return type + '[id=' + id + ']';
	}

	return {
		/**
		 * Show the results obtained from an ajax call.  If the call was made previously, the
		 * previous are returned from the cache.
		 *
		 * link: the link to follow for data retrieval.  should be an anchor object
		 * resultSelector: the id of the field where results should be placed after retrieval
		 * isTLM: flags this link as a top level menu
		 */
		showResults : function(link, resultSelector, isTLM)
		{
			// throw out the wait sign
			jQuery('body').css('cursor', 'wait');

			var resultArea = jQuery(resultSelector);

			// if no cached content found, request content from server
			if (resultArea.html())
			{
				resultArea.show();
			}
			else
			{
				// setup the function that initiates the AJAX request
				var updater = RSF.getAJAXLinkUpdater(link, function(results)
					{
						// put the results on the page
						resultArea.html(results).show();
						// set the checkboxes based on the group level checkbox
						RcptSelect.toggleSelectAll(jQuery(link).siblings('input[type=checkbox]:first').attr('id'));
					});

				// update the page
				updater();
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
			jQuery('body').css('cursor', 'default');
		},

		showOther: function()
		{
			jQuery('#otherRecipientsDiv').show();
			jQuery('#otherRecipientsLink').hide();
		},

		showIndividuals: function(link, usersAreaId, selectLinkId, collapseLinkId)
		{
			RcptSelect.showResults(link, _safeId('div', usersAreaId));
			jQuery(_safeId('a', selectLinkId)).hide();
			jQuery(_safeId('a', collapseLinkId)).show();
		},

		hideIndividuals: function(usersAreaId, selectLinkId, collapseLinkId)
		{
			jQuery(_safeId('a', collapseLinkId) + ', ' + _safeId('div', usersAreaId)).hide();
			jQuery(_safeId('a', selectLinkId)).show();
		},

		toggleSelectAll: function(checkboxId)
		{
			var checkbox = _safeId('input', checkboxId);
			var checked = jQuery(checkbox).is(":checked");
			jQuery('input[type=checkbox]', checkbox + ' ~ div:first').attr('checked', checked);
		},

		toggleIndividual: function(checkboxId)
		{
			var checkbox = jQuery(_safeId('input', checkboxId));
			/*
			the individual checkboxes are nested down some divs.
			the structure looks like:
			<input id=<selectAll> />
			<div> <-- parent -->
			  <div> <-- parent -->
			    <div> <-- parent -->
			      <input id=<individual> />
			    </div>
			  </div>
			</div>
			*/
			var selectAll = checkbox.parent().parent().parent().siblings('input[type=checkbox]');
			if (!checkbox.is(':checked'))
			{
				selectAll.attr('checked', false);
			}
			else
			{
				/*
				have to go up 2 parents because each checkbox is held in a div of divs
				<div>
				  <div>
				    <input type=checkbox />
				    <label />
				  </div>
				  <div>
				    <input type=checkbox />
				    <label />
				  </div>
				</div>
				*/
				var allChecked = true;
				jQuery('input[type=checkbox]', checkbox.parent().parent()).each(function()
					{
						if (!this.checked)
						{
							allChecked = false;
							return false;
						}
					});
				if (allChecked)
				{
					selectAll.attr('checked', true);
				}
			}
		}
	}; // end return
}(); // end namespace