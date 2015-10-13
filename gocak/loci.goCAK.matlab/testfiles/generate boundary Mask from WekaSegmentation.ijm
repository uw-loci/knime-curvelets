
run("Close All");

//for (n = 0; n < 2; n=n+1)
//{
	
//if (n==0){
//	np = "Pos";	
//} else {
//	np = "Neg";
//}

//dir1 = "Z:\\bredfeldt\\Conklin data - Invasive tissue microarray\\FijiResults\\Segmentation\\Weka\\results_1B_part1\\";
dir1 = getDirectory("Choose the probability map  directory");
list1 = getFileList(dir1);
//dir2 = "Z:\\bredfeldt\\Conklin data - Invasive tissue microarray\\Slide 1B\\Slide 1B\\"
dir2 = getDirectory("Choose the original image  directory");
list2 = getFileList(dir2);
//outpath = "Z:\\bredfeldt\\Conklin data - Invasive tissue microarray\\FijiResults\\Segmentation\\Weka\\results_1B_part2\\";
outpath = getDirectory("Choose the mask for CurveAlign  directory");

for (k = 0; k <  list1.length; k = k + 1)
//for (k = 7; k < 9; k = k + 1)
{
	name = list1[k];
	open(dir1 + name,1); //only open first image
	
	name = replace(name,'_HE',''); //remove _HE from the filename
	run("Gaussian Blur...", "sigma=6");
	
	setThreshold(0.20, 1.0);
	run("Create Mask");
	
	/*//return;
	//run("Invert");
	//run("Invert LUT");
	run("Analyze Particles...", "size=0-Infinity circularity=0.00-1.00 show=Masks include");	
	//return;
	run("Gaussian Blur...", "sigma=16");
	setThreshold(40, 255);
	//return;
	run("Create Mask");
	
	run("Analyze Particles...", "size=50-Infinity circularity=0.00-1.00 show=Masks");
	//return;
	//run("Convert to Mask", "method=Default");	
	rename("epithelial_clusters");	
	//return;
	//run("Duplicate...", "title=[epithelial_skeleton]");
	//run("Skeletonize (2D/3D)");
	//run("Invert LUT");
	//save(outpath+"sboundary for "+name+".tif");
	selectWindow("epithelial_clusters");
	*/
	run("Invert LUT");	
	
	save(outpath+"mask for "+name+".tif");
	//selectWindow("epithelial_clusters");		
	run("Outline");	
	/*
	//return;
	rename("epithelial_cluster_outlines");	
	run("Invert");	
	save(outpath+"boundary for "+name+".tif");
	run("Invert LUT");
	wait(100);
	open(dir2 + list2[k]);
	rename("he");
	run("Split Channels");
	run("Merge Channels...", "c1=[he (red)] c2=[he (green)] c3=[he (blue)] c7=epithelial_cluster_outlines create");
	//run("Add Image...", "image=epithelial_cluster_outlines x=0 y=0 opacity=50");
	//return;
	run("RGB Color");
	save(outpath+"weka_epithelial_over_"+name);
	wait(100);
	//return;
	*/
	run("Close All");
	
}

