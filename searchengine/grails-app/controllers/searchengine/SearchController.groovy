package searchengine

class SearchController {
	// logic and methods

	// Solr URL
	String urlString = "http://localhost:8983/solr/"
	def solr = new org.apache.solr.client.solrj.impl.HttpSolrServer(urlString)
	def uQ
	def myresponse = solr.query(uQ)
	
	// Main content
	def index() {
		def allSearch = Search.list()		// query data from search model db
		[allSearch:allSearch] // send queried data to view
	}

	def myquery() {
		def solrparams = new org.apache.solr.client.solrj.SolrQuery()
		uQ = params.address.replaceAll(' ','+')  // replace spaces in query with '+' for solr
		uQ = uQ.replaceAll("'",'?')							 // replace apostrophes with ?
		solrparams.set("q", uQ)	 								 // set query
		solrparams.set("defType", "edismax")		 // set solr to run as edismax
		solrparams.set("hl", "true")						 // turn highlighting on
		solrparams.set("fl", "professor education") 			 // return fields
		solrparams.set("qf", "professor^20.0 schools^10.0 degrees^5.0 country^0.3 education^5.0 courses^1.0") // multiple field query and boost
		myresponse = solr.query(solrparams)		
		render myresponse
	}

	// Query all solr data
	def all() {
		def params = new org.apache.solr.client.solrj.SolrQuery()
		params.set("q", "*")
		def response = solr.query(params)
		//render response
	//	def jArray = new org.codehaus.groovy.grails.web.json.JSONArray(response)
	//	render jArray
	  render response
	}
	
	//
	
}
