package searchengine

class SearchController {

	/* Global Variables */
	String urlString = "http://localhost:8983/solr/"
	def solr = new org.apache.solr.client.solrj.impl.HttpSolrServer(urlString)
	def TOTAL_UNIVERSITIES = 852	// const for scrape total, used for rank calculation
	def startPageNum = 0		// for multipage
 	def currentPageNum = 1	// for multipage

	/* Main */
	def index() {
	}

	/* Initial Query */
	def mainQuery() {		
		
		/* Local Variables */
		def solrparams = new org.apache.solr.client.solrj.SolrQuery()
		def uQ
		def response
		def allResults = []			// collection for concatenation of all prof maps

		//if uQ is specified means its a subquery
		if (!uQ)
			uQ = params.address				// user query input value from gsp

		solrparams.setQuery(uQ)
		solrparams.set("qf","""
										professor
										position
										courses
										education
										country
										""")										 // searchable fields
		solrparams.set("q.op", "OR")						 // allow in-exact matches
		solrparams.set("defType", "edismax")		 // set solr to run as edismax
		solrparams.set("start",startPageNum)		 // set results starting point for multipage
		
		response = solr.query(solrparams)				 // main query raw header and results
						 
		def doclist = response.getResults()			 // main query results
		
		/* Unsuccessful search */
		if (doclist.getNumFound() == 0)
			render "<br />Sorry, I could not find any matches for <b>$uQ</b><br /><br />"
	  else {
      render "<br />Results found: ${doclist.getNumFound()} ---- Page # $currentPageNum<br /><br />"
		}

		/* Loop every result doc in main results */		
		for (org.apache.solr.common.SolrDocument doc : doclist) {
			
			def id = doc.getFieldValue("id")								// single unique value
			def name = doc.getFieldValue("professor")				// single value
			def plink = doc.getFieldValue("website")				// single unique value
			def position = doc.getFieldValue("position")		// single value
			def courses = doc.getFieldValues("courses")			// multivalue
			def schools = doc.getFieldValues("schools")			// multivalue
			def education = doc.getFieldValues("education")	// multivalue
			def result = [name:"",
									 plink:"",
								position:"",
									course:"",
									school:"",
									 slink:"",
									 ranks:"",
									 prank:"",
							 education:""]		// map data for each prof
			
			/* Professor name and profile website	*/
			result.name = name	// set name data
			result.plink = plink	// set plink data
			
			/* Professor position, if exists	*/
			if (position != null) {
				result.position = position	// set position data
			}
			
			/* Courses taught, if any */
			result.course = courses
			
			/* Schools Attended */
			def schoollist				// nested query results used to find school rank
			def linklist					// nested query results used to find school link
			def uniName						// school name key for sub query									[single unique value]
			def uniRank						// school rank match															[single value]
			def uniLink						// school site key for sub query									[single unique value]
			def actualUnis = []		// original school names from prof profiles				[multivalued]
			def pooledRanks = []	// school rank matches for later retrieval				[multivalued]
			def pooledLinks = []	// school link matches for later retrieval				[multivalued]
			def uniqueUnis = []		// used to remove duplicate schools for a prof		[multivalued]
			
			/* Match each school to rank data and website link */			
			schools.each {
								
				def cut = it.replaceAll('Computer','')	// clean the school query values
				cut = cut.replaceAll('Canada','')
				cut = cut.replaceAll('Science','')
				cut = cut.replaceAll('\\-',' ')				
												
				if (cut.matches('.*\\w.*')) {
					actualUnis.add(cut)														// store actual school names
				}
				
				/* School Ranks Subquery */
				def schoolparams = new org.apache.solr.client.solrj.SolrQuery() // subquery params
				schoolparams.setQuery(cut)							// cleaned values for query
				schoolparams.set("q.op", "AND")					// exact match
				schoolparams.set("rows",1)							// return first match only
				schoolparams.set("defType", "edismax")
				schoolparams.set("qf", "uniName")				// query school name->rank keys only				
				schoollist = solr.query(schoolparams).getResults()	// subquery results			
				
				/* Loop each school in results */				
				for (org.apache.solr.common.SolrDocument school : schoollist) {
					
					uniName = school.getFieldValue("uniName")		// school name->rank key
					uniqueUnis.add(uniName)											// append school name keys
					uniqueUnis = uniqueUnis.unique()						// remove duplicate schools
					
					uniRank = school.getFieldValue("uniRank")		// school rank match
					pooledRanks.add(uniRank)										// append ranks
					pooledRanks = pooledRanks.unique()					// remove duplicate ranks
					result.ranks = pooledRanks									// set school rank data
					
					/* School Links Subquery */
					def linkparams = new org.apache.solr.client.solrj.SolrQuery()	// subquery params
					linkparams.setQuery(cut)										// cleaned values for query
					linkparams.set("q.op", "AND")								// exact match
					linkparams.set("rows",1)										// return first match only
					linkparams.set("defType", "edismax")
					linkparams.set("qf", "university")					// query school name->link keys only
					linklist = solr.query(linkparams).getResults()	// subquery results
					
					/* Loop each link in results */
					for (org.apache.solr.common.SolrDocument link : linklist) {						
												
						uniLink = link.getFieldValue("uniLink")		// school link
						pooledLinks.add(uniLink)									// append school links
						pooledLinks = pooledLinks.unique()				// remove duplicates
													
					} // end for link:linklist
					
					result.slink = pooledLinks									// set school link data
					
				}	// end for school:schoollist
			}	// end schools.each
			
			result.school = uniqueUnis
			
			/* Education */
			result.education = education
			
			/* Calculate Ranks */
			result.prank = calculateRanks(pooledRanks)
			
			/* Add to Results */
			allResults.add(result)

		} // end for (doc : doclist)
		
		/* to View */
		show(allResults)
		
		/* get SchoolData to the view for the js */
		pass(allResults)		
		
		/* for Relationships */
		if (findSimilarities(allResults, 0) == 1)
			findSimilarities(allResults, 2)
		
		/* Multi-Paging */
	  def currentPage = request.getForwardURI()
    multiPage(doclist.getNumFound(), currentPage, uQ)
			
	} // end mainQuery
	
	// pass school data to view for Maps API
	def pass(e) {
		def sflag = 0
		def sch
		def cnt = [:]
		e.eachWithIndex { v, i ->
			if (v.school) {
				sch += v.school.toString()
				sflag = 1
				cnt.putAt(i,v.school)
				i++
			}
		}
		if (sflag == 1)
			render (template:"/result",model:[sch:cnt.toString()])
	}
	
    /* multi pages */
    def multiPage(numPages, currentURI, uQ){

        def filteredURI = []
        def l = currentURI.split("/")

        //remove the last element
        int i = 0
        l.each{
           if(i != l.size()-1)
               filteredURI.add(it)
            i+= 1
        }

        currentURI = filteredURI.join('/')

        int pages = getPages(numPages)
        def queryString = uQ.replaceAll(" ","%20")

        render "<br/>"
        render "<br/>"
        render "<link rel='stylesheet' href='/searchengine/static/css/search.css' type='text/css'>"

        render "<center>"
        render "<div class='rpage'>"

        if(pages > 1){
            (1..pages).each{
                def link = "${currentURI}/page?q=${queryString};p=$it;c=$startPageNum"
                render "<a href=$link style='text-decoration:none'>  $it  <a/>"
            }
        }

        render "</div>"
        render "</center>"
        render "<br/>"
        render "<br/>"
    }

	/* Setter for simple string values */
	def setData(a) {
		def b
		a.each {
			if (!it.isEmpty()) {
				if (it == a.first())
					b = it
				else if (it == a.last()) {
					b = b + it
				} else
					b = b + it
			}
		}
		return b
	}
	
	/* Calculate and return the professor's prestige */
	def calculateRanks(ranklist) {
		if (ranklist.size() > 0) {
			def totalRank = ranklist.sum()
			def rank = totalRank / ranklist.size()
			def pct = (1 - (rank / TOTAL_UNIVERSITIES )) * 100
			pct = pct.setScale(1, BigDecimal.ROUND_HALF_UP).toString()
			return pct
		}
	}

	/* Cross-section analyse */	
	def findSimilarities(e, flag) {
		
		def p 							// prof name
		def cid 						// course id
		def map = [:]				// prof label : course ids
		def map2 = [:]			// prof label : school name
				
		/* MAP Prof Name : Course Ids; Prof Name : School Name */		
		e.eachWithIndex { v1, i1 -> 
			def r																// prof + course id as key
			def s																// prof + school as key
			p = "$v1.name"											// prof name			
			tokMapper(p, cid, map, r, v1.course, i1)	// make maps of prof name : course id
			strMapper(p, map2, s, v1.school, i1)			// make maps of prof name : school name
		}
		
		if (flag == 2)
			render "<div class='cell'><ul><li><h3>Matching Relationships</h3><ul>"
	
		/* Iterate Current and Complement Maps */
		def count = 0
		def cmap = [:]
		e.each {
			if (it.course) {
				def position = count
				count += it.course.size()
			
				def current = map.take(count)									
				def complement = map.take(position) << map.drop(count)
			
				if (position > 0) {
					current = current.drop(position)
					complement = complement.drop(position)
				}

				current.each { ku, vu ->
					complement.each{ ko, vo ->
						if ((vu == vo) && (flag == 0)) {
							flag = 1											// first iteration to draw div in main loop
						}
					
						if ((vu == vo) && (flag == 2)) {
							def uName = ku.takeWhile{ it != "0"}
							def oName = ko.takeWhile{ it != "0"}

	//						cmap.putAt(vu,uName + ', ' + oName)
	//						render "$cmap <br /><br />"						
	//						render "${cmap.groupBy{ it.key.replaceAll('\\W',' ') }}"

							render "<li>$uName and $oName both teach ${vu.replaceAll('\\W',' ')}</li>"
						}	// end if					
					}	// end.complement.each
				}	// end.current.each
			} // end if it.course
		}	// end e.each
		count = 0
		cmap = [:]
		e.each {
			if (it.school) {
				def position = count
				count += it.school.size()

				def current = map2.take(count)									
				def complement = map2.take(position) << map2.drop(count)

				if (position > 0) {
					current = current.drop(position)
					complement = complement.drop(position)
				}

				current.each { ku, vu ->
					complement.each{ ko, vo ->
						if ((vu == vo) && (flag == 0)) {
							flag = 1											// first iteration to draw div in main loop
						}

						if ((vu == vo) && (flag == 2)) {
							def uName = ku.takeWhile{ it != "0"}
							def oName = ko.takeWhile{ it != "0"}

	//						cmap.putAt(vu,uName + ', ' + oName)
	//						render "$cmap <br /><br />"						
	//						render "${cmap.groupBy{ it.key.replaceAll('\\W',' ') }}"

							render "<li>$uName and $oName both attended ${vu.replaceAll('\\W',' ')}</li>"
						}	// end if					
					}	// end.complement.each
				}	// end.current.each
			} // end if it.school
		}	// end e.each		
		if (flag == 2)
			render "</ul></li></ul></div>"
		if (flag == 1)
			return flag
	}
	
	private tokMapper(prof, eid, emap, ckey, v, i) {
		if (v) {
			v.eachWithIndex { v2, i2 -> ckey = "$prof" + 0 + "$i$i2"
				eid = "${v2.tokenize().take(2)}"		// truncate label to id				
				emap.putAt(ckey,eid)								// map each prof key with token ids
			}
		}
	}
	
	private strMapper(prof, emap, ckey, v, i) {
		if (v) {
			v.eachWithIndex { v2, i2 -> ckey = "$prof" + 0 + "$i$i2"
				emap.putAt(ckey,v2)						// map each prof key with str element
			}
		}
	}
	
	/* to View */
	def show(e) {
		e.each { f ->
			render "<span class='name'><a href='$f.plink' target='_blank'>$f.name</a></span>"
			if (f.position) {
			 render " is a SFU $f.position"
			}
			if (f.course) {
				f.course.each {
					if (it == f.course.first())
						render " who teaches: <br /><span class='course'>$it"
					else if (it == f.course.last()) {
						render ", and $it.</span>"
					} else
						render ", $it"
				}
			}
			if (f.school) {
				render "<span class='smindent'>${f.name.takeWhile{ it != ' ' }} attended:</span>"
				if (f.ranks) {
					f.school.eachWithIndex {g, i ->
						render "<span class='indent'><a href='${f.slink[i]}' target='_blank' class='school'>$g</a>, with a world ranking of: ${f.ranks[i]}</span>"
					}
				}
			}
			if (f.education) {
				render "<span class='smindent'>${f.name.takeWhile{ it != ' ' }} holds the following credentials:</span>"
				f.education.each {
					render "<span class='indent'>$it</span>"
				}
			}
			if (f.prank) {
				render "<span class='prestige'>${f.name.takeWhile{ it != ' ' }}'s world prestige is: <em>$f.prank</em></span>"
			}
			render "<br />"
		}
		render "<br />"
	}

    def page(){
        def r = request.getQueryString().split(";")
        int num = -1
        def query = r[0].split('=')
        if (r.size() > 1)               //check if 2nd argument is specified
        {
            def page = r[1].split('=')
            num = page[1] as int
            currentPageNum = num
        }

        if(num != -1)
            startPageNum = ((num-1)*10)
        else
            startPageNum = 0
        uQ = query[1].replaceAll("%20"," ")


        /* ghetto rendering of the next few pages */

        render "<title>Prestige query :: $uQ</title> "
        render "<link rel='shortcut icon' href='images/favicon.ico' >"

        render "<div class='wrapper'>"
        render "<div class='content'>"
        render "<div class='nav'>"
          render "<center>"
          render "<a href='/searchengine/search/index'>Prestige :: Engine</a> |"
	      render "<a href='https://docs.google.com/document/d/1bfjZXHmQLMC_q7rIz6f-X6kbokwQAy1HjQPIxf53K8g/edit?usp=sharing' target='_blank'>Documentation</a> |"
          render "<a href='/searchengine/about/index'>About the Team</a> |"
	      render "<a href='/searchengine/ranking/index'>University Ranking</a>"
          render "</center>"
        render "</div>"

        /*  query box */
        render "<br/>"
        render "<br/>"
        render "<form name='input' action='subquery' method='post'>"
        render "<input type='text' name='newQuery' value='$uQ' >"
        render "<input type='submit' value='Tarot'>"
        render "</form>"

        render "<div id='results'>"
            mainQuery()
        render "</div>"
        render "</div>"

    }

    /* action for the querybox in 'page' results */
    def subquery(){
        def r = request.getParameter('newQuery')
        redirect(uri:"/search/page?q=$r")
    }

  //HELPER FUNCTION TO DETERMINE HOW RESULT PAGES
  private getPages(pages){
      int numPages = pages/10
      if(pages%10 > 0)
          numPages += 1
      return numPages
  }
	
}
