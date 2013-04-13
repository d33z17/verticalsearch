package searchengine

class SearchController {
	// logic and methods

	// Solr URL
	String urlString = "http://localhost:8983/solr/"
	def solr = new org.apache.solr.client.solrj.impl.HttpSolrServer(urlString)
	def uQ
	def response = solr.query(uQ)
	
	// Main content
	def index() {
		def allSearch = Search.list()		// query data from search model db
		[allSearch:allSearch] // send queried data to view
	}

	def myquery() {		
		def solrparams = new org.apache.solr.client.solrj.SolrQuery()
		uQ = params.address
		uQ = uQ.replaceAll("'",'?')							 // replace apostrophes with ?
		solrparams.setQuery(uQ)
		solrparams.set("q.op", "OR")						 // allow in-exact matches
		solrparams.set("defType", "edismax")		 // set solr to run as edismax
		solrparams.set("rows",11)								 // set number results returned
																						 // multiple field query and boost
		solrparams.set("qf", """\
													professor^20.0
													position^15.0
													schools^10.0 
													degrees^5.0 
													country^0.3 
													education^5.0 
													courses^1.0
													""")
		response = solr.query(solrparams)
						
		def doclist = response.getResults()
		
		if (doclist.getNumFound() == 0)
			render "Sorry, I could not find any matches for " + uQ + "<br /><br />"
		
		def prof[]
		
		for (org.apache.solr.common.SolrDocument doc : doclist) {
						
			render "<a href='http://'>" + doc.getFieldValues("professor")[0].toString() + "</a>"
			
			doc.getFieldValues("professor").each {
				
				doc.getFieldValues("position").collect {
					if (it.matches(".*\\w.*"))
						render ", " + it
				}
				
				doc.getFieldValues("courses").collect {
					if (it.matches(".*\\w.*"))
						render "<br />" + it
				}
				
				doc.getFieldValues("schools").collect {
					if (it.matches(".*\\w.*"))
						render "<br /><a href='http://'>" + it + "</a>"
				}
				
				doc.getFieldValues("education").collect {
					if (it.matches(".*\\w.*"))			// regex to match alphanumeric, \\ = \ in java strings; used to omit blank education fields
						render "<br />" + it
				}
			}
			render("<br /><br />")						
		}		
	}
	
}
