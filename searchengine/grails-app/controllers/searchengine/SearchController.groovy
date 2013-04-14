package searchengine

class SearchController {
	// logic and methods

	// Solr URL
	String urlString = "http://localhost:8983/solr/"
	def solr = new org.apache.solr.client.solrj.impl.HttpSolrServer(urlString)
	def uQ
	def response
	
	// Main content
	def index() {
		def allSearch = Search.list()		// query data from search model db
		[allSearch:allSearch] // send queried data to view
	}

	def myquery() {		
		
		/* Initial Query */
		def solrparams = new org.apache.solr.client.solrj.SolrQuery()
		uQ = params.address
		uQ = uQ.replaceAll("'",'?')							 // replace apostrophes with ?
		solrparams.setQuery(uQ)
		solrparams.set("q.op", "OR")						 // allow in-exact matches
		solrparams.set("defType", "edismax")		 // set solr to run as edismax
		solrparams.set("rows",11)								 // set number results returned
																						 // multiple field query and boost
		solrparams.set("qf", """\
													id^1.0
													professor^20.0
													website^15.0
													position^15.0
													schools^10.0
													degrees^5.0 
													country^0.3 
													education^5.0 
													courses^1.0
													""")
		response = solr.query(solrparams)		

//		render response	// debug
						 
		def doclist = response.getResults()
		
		/* no results output message */
		if (doclist.getNumFound() == 0)
			render "Sorry, I could not find any matches for " + uQ + "<br /><br />"

		/* iterate each matched result in the result list */
		def profEduc = new Object[doclist.getNumFound()][]
		render profEduc
		for (org.apache.solr.common.SolrDocument doc : doclist) {
			
			def i = 1
			def id = doc.getFieldValue("id")
			def name = doc.getFieldValue("professor")
			def link = doc.getFieldValue("website")
			def position = doc.getFieldValue("position")
			def courses = doc.getFieldValues("courses")
			def schools = doc.getFieldValues("schools")
			
			profEduc[id][i] = name
			
			// professor name and profile website			
			render "<span class='name'><a href='" + link + "'>" + name + "</a></span>"			
			
			// professor position if exists	
			if (position != null)
				render ", " + position
			
			// courses taught if any
			courses.collect {
				if (!it.isEmpty())
					render "<br />" + it
			}
			
			// schools attended
			def totalRank = 0
			def count = 0
			def schoollist
			def uniName
			schools.each {
				
				if (!it.isEmpty()) {
					count ++
				}
				
				render "<br /><a href='http://'>" + it + "</a>"

				def schoolparams = new org.apache.solr.client.solrj.SolrQuery()
				schoolparams.clear()
				def cut = it.replaceAll('of','')
				schoolparams.setQuery(cut)
				schoolparams.set("q.op", "AND")
				schoolparams.set("defType", "edismax")
				schoolparams.set("mm",3)
				schoolparams.set("qf", """\
				 													schools^20.0
																	uniRank^10.0
																	uniName^15.0
																	""")																	
				schoollist = solr.query(schoolparams).getResults()
				for (org.apache.solr.common.SolrDocument school : schoollist) {
					
					uniName = school.getFieldValues("uniName")
					
					if (uniName != null) {
						render ", world comp sci ranking: " + school.getFieldValue("uniRank")
						totalRank += school.getFieldValue("uniRank")
					}
				}
			}
			render "<br />" + name + "'s comp sci ranking is: " + Math.round(totalRank/count)
				
			doc.getFieldValues("education").collect {
				if (!it.isEmpty())
					render "<br />" + it
			}
			
			render "<br />"
			render schoollist

		} // end for (doc : doclist)
	}
	
}
