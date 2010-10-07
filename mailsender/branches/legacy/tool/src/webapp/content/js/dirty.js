var Dirty = function()
{
	/**
	 * Collect all fossils on the page along with the user input fields
	 */
	function collectFossils()
	{
		var fossilex =  /^(.*)-fossil$/;
		var fossils = new Object();
		var elements = document.getElementsByTagName('input');
		for (var i = 0; i < elements.length; i++)
		{
			var element = elements[i];
			// see if name exists and matches regex
			if (element.name)
			{
				var matches = element.name.match(fossilex);
				if (matches != null)
				{
					// use the name sans '-fossil' to store the element
					// this saves having to parse the field name again
					// later in processing.
					fossils[matches[1]] = element;
				}
			}
		}
		return fossils;
	}

	return {
		isDirty : function()
		{
			var dirty = false;
			var fossilElements = collectFossils();
			var inputs = new Array();
			for (propName in fossilElements)
			{
				var fossilElement = fossilElements[propName];
				var fossil = RSF.parseFossil(fossilElement.value);
				var inputs = document.getElementsByName(propName);
				for (var j = 0; j < inputs.length; j++)
				{
					var input = inputs[j];
					if (((input.type == 'checkbox') && (input.checked != (fossil.oldvalue == "true")))
							|| ((input.type == 'select') && (input.options[input.selectedIndex].value != fossil.oldvalue))
							|| ((input.type == 'radio') && (input.checked) && (input.value != fossil.oldvalue))
							|| ((input.type == 'text' || input.type == 'textarea' || input.type == 'password') && (input.value != fossil.oldvalue)))
					{
						dirty = true;
						break;
					}
				}
				if (dirty) break;
			}
			return dirty;
		}
	}; // end return
}(); // end namespace
