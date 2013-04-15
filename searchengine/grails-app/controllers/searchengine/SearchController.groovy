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
		solrparams.setQuery(uQ)
		solrparams.set("qf","""
										professor
										position
										courses
										education
										country
										""")										 // searchable fields
		solrparams.set("q.op", "OR")						 // allow in-exact matches
//		solrparams.set("mm",2)									 // minimum match 2 elements since in-exact
		solrparams.set("defType", "edismax")		 // set solr to run as edismax
		
		response = solr.query(solrparams)		

//		render response	// debug
						 
		def doclist = response.getResults()
		
		/* no results output message */
		if (doclist.getNumFound() == 0)
			render "Sorry, I could not find any matches for " + uQ + "<br /><br />"

		/* iterate each matched result in the result list */
		def allResults = []
		
		for (org.apache.solr.common.SolrDocument doc : doclist) {
			
			def i = 1
			def id = doc.getFieldValue("id")
			def name = doc.getFieldValue("professor")
			def plink = doc.getFieldValue("website")
			def position = doc.getFieldValue("position")
			def courses = doc.getFieldValues("courses")
			def schools = doc.getFieldValues("schools")
			def result = [name:"",plink:"",position:"",course:"",school:""]

//render "SCHOOLS: " + schools
			
			// professor name and profile website			
			render "<span class='name'><a href='" + plink + "'>" + name + "</a></span>"
			result.name = name
			
			// professor position if exists	
			if (position != null) {
				render ", is an SFU " + position + " who"
				result.position = position
			}
			
			// courses taught if any
			courses.each {
				if (!it.isEmpty()) {
					result.course = it
					if (it == courses.first())
						render " teaches: "
					else if (it == courses.last())
					  render "and " + it + "."
					else
						render it + ", "
				}
			}
			
			// schools attended
			def totalRank = 0
			def count = 0
			def schoollist
			def uniName
			def uniLink
			def uniqueUnis = []
						
			schools.each {
				
				def schoolparams = new org.apache.solr.client.solrj.SolrQuery()
				def cut = it.replaceAll('Computer','')
				cut = cut.replaceAll('Canada','')
				cut = cut.replaceAll('Science','')
				cut = cut.replaceAll('\\-',' ')				
												
				if (cut.matches('.*\\w.*')) {
					if (it == schools.first())
						result.school = cut
					else
						result.school = result.school + "," + cut
				}
				
				schoolparams.setQuery(cut)
				schoolparams.set("q.op", "AND")
				schoolparams.set("rows",1)
				schoolparams.set("defType", "edismax")
				schoolparams.set("qf", "uniName")
				
//				render "NESTED QUERY IS: " + schoolparams
				
				schoollist = solr.query(schoolparams).getResults()
				
//				render "SOLR MATCHES: " + schoollist
								
				for (org.apache.solr.common.SolrDocument school : schoollist) {
					uniName = school.getFieldValue("uniName")
					uniqueUnis.add(uniName)
					render "NUMBER UNIS: " + uniqueUnis
					
					def linkparams = new org.apache.solr.client.solrj.SolrQuery()
					linkparams.setQuery(cut)
					linkparams.set("q.op", "AND")
					linkparams.set("rows",1)
					linkparams.set("defType", "edismax")
					linkparams.set("qf", "university")
					def linklist = solr.query(linkparams).getResults()
					
					for (org.apache.solr.common.SolrDocument link : linklist) {
						uniLink = link.getFieldValue("uniLink")
					
//					render uniName
//					uniLink = school.getFieldValue("uniLink")
//					render "UNILINKS: " + uniLink
					
						if (!uniName.isEmpty()) {
							render "<br /><a href='http://" + uniLink + "'>" + cut + "</a>, world ranking: " + school.getFieldValue("uniRank")
							totalRank += school.getFieldValue("uniRank")
							count++
						} else
								count--					
					} // end for link:linklist
				}	// end school.each
			}
			
			if (count > 0) {
				def rank = totalRank / count
				def pct = (1 - (rank / 852 )) * 100
				render "<br />" + name + "'s prestigiousness: " + pct.setScale(1, BigDecimal.ROUND_HALF_UP).toString()
				render "TOTALRANK: " + totalRank + " COUNT: " + count + " RANK: " + rank + " "
			}
			
			doc.getFieldValues("education").collect {
				if (!it.matches("\\S+"))
					render "<br />" + it
			}
			
//			render "RESULTS: " + result
			allResults.add(result)
			
			render "<br /><br />"

		} // end for (doc : doclist)
		
		render 
		
		// TO BE OUTPUT: *plink *name is an SFU *position who teaches *course: ... *school *slink *srank *prank *education
		
		/* Cross-section analyse */
//		render "EACH_MATCH: " + allResults.eachMatch( it.get('course'), '/.*\\d/.*', )
//		render "FIND_RESULTS: " + allResults.findResults{ it.get('course') == " " ?  : null }
		
		
	} // end myQuery
	
}
