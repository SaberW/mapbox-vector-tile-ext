sample
===
* 注释

	select #use("cols")# from locations  where  #use("condition")#

cols
===
	dev_id,class_id,dev_name,parent_id,vlevel_code,mrid,buro,subburo,site_id,site_type,bay_id,last_modify_time,remark,run_status_code,ownership_code,dev_model,factory,guid,depart,feeder_id,location_type,planid,projectid,operationid,tense,status,grid_id,price

updateSample
===
	
	dev_id=#devId#,class_id=#classId#,dev_name=#devName#,parent_id=#parentId#,vlevel_code=#vlevelCode#,mrid=#mrid#,buro=#buro#,subburo=#subburo#,site_id=#siteId#,site_type=#siteType#,bay_id=#bayId#,last_modify_time=#lastModifyTime#,remark=#remark#,run_status_code=#runStatusCode#,ownership_code=#ownershipCode#,dev_model=#devModel#,factory=#factory#,guid=#guid#,depart=#depart#,feeder_id=#feederId#,location_type=#locationType#,planid=#planid#,projectid=#projectid#,operationid=#operationid#,tense=#tense#,status=#status#,grid_id=#gridId#,price=#price#

condition
===

	1 = 1  
	@if(!isEmpty(devId)){
	 and dev_id=#devId#
	@}
	@if(!isEmpty(classId)){
	 and class_id=#classId#
	@}
	@if(!isEmpty(devName)){
	 and dev_name=#devName#
	@}
	@if(!isEmpty(parentId)){
	 and parent_id=#parentId#
	@}
	@if(!isEmpty(vlevelCode)){
	 and vlevel_code=#vlevelCode#
	@}
	@if(!isEmpty(mrid)){
	 and mrid=#mrid#
	@}
	@if(!isEmpty(buro)){
	 and buro=#buro#
	@}
	@if(!isEmpty(subburo)){
	 and subburo=#subburo#
	@}
	@if(!isEmpty(siteId)){
	 and site_id=#siteId#
	@}
	@if(!isEmpty(siteType)){
	 and site_type=#siteType#
	@}
	@if(!isEmpty(bayId)){
	 and bay_id=#bayId#
	@}
	@if(!isEmpty(lastModifyTime)){
	 and last_modify_time=#lastModifyTime#
	@}
	@if(!isEmpty(remark)){
	 and remark=#remark#
	@}
	@if(!isEmpty(runStatusCode)){
	 and run_status_code=#runStatusCode#
	@}
	@if(!isEmpty(ownershipCode)){
	 and ownership_code=#ownershipCode#
	@}
	@if(!isEmpty(devModel)){
	 and dev_model=#devModel#
	@}
	@if(!isEmpty(factory)){
	 and factory=#factory#
	@}
	@if(!isEmpty(guid)){
	 and guid=#guid#
	@}
	@if(!isEmpty(depart)){
	 and depart=#depart#
	@}
	@if(!isEmpty(feederId)){
	 and feeder_id=#feederId#
	@}
	@if(!isEmpty(locationType)){
	 and location_type=#locationType#
	@}
	@if(!isEmpty(planid)){
	 and planid=#planid#
	@}
	@if(!isEmpty(projectid)){
	 and projectid=#projectid#
	@}
	@if(!isEmpty(operationid)){
	 and operationid=#operationid#
	@}
	@if(!isEmpty(tense)){
	 and tense=#tense#
	@}
	@if(!isEmpty(status)){
	 and status=#status#
	@}
	@if(!isEmpty(gridId)){
	 and grid_id=#gridId#
	@}
	@if(!isEmpty(price)){
	 and price=#price#
	@}