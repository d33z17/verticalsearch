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
		solrparams.set("q.op", "OR")						 				// allow in-exact matches
		solrparams.set("defType", "edismax")		 				// set solr to run as edismax
		solrparams.set("qf", "professor^20.0 schools^10.0 degrees^5.0 country^0.3 education^5.0 courses^1.0") // multiple field query and boost
		response = solr.query(solrparams)
		
		def doclist = response.getResults()
		
		for (org.apache.solr.common.SolrDocument doc : doclist) {
			render doc.getFieldValues("professor")[0].toString() + " attended <ol>"
			doc.getFieldValues("professor").each {
				doc.getFieldValues("education").collect {
					if (it.matches(".*\\w.*"))
						render "<li>" + it + "</li>"
				}
			}
			render("</ol><br />")						
		}		
	}
	
}
