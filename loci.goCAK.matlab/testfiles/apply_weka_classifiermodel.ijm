
run("Close All");
run("Leaf (36K)");
// start plugin
run("Advanced Weka Segmentation");
 
// wait for the plugin to load
wait(3000);
selectWindow("Trainable Weka Segmentation v2.1.0");
modDir = getDirectory("Choose the classifer model  directory");
list0 = getFileList(modDir);
classFile = modDir+list0[0];
//classFile = "C:\\Users\\yuming\\Desktop\\Weka_mask_creation\\classifiertest2.model";
//classFile = getFiles(
print("Loading classifier: " + classFile);
call("trainableSegmentation.Weka_Segmentation.loadClassifier", classFile);
//inDir = "C:\\Users\\yuming\\Desktop\\Weka_mask_creation\\testimages\\";  // original image folder
//outDir = "C:\\Users\\yuming\\Desktop\\Weka_mask_creation\\testimages\\part1\\"; // probability map folder

inDir = getDirectory("Choose the original image  directory");
outDir = getDirectory("Choose the probability map  directory");

list1 = getFileList(inDir);

for (k = 0; k <  list1.length; k = k + 1)
//for (k = 0; k <  1; k = k + 1)
{
	print("Applying classifier to " + list1[k]);
	call("trainableSegmentation.Weka_Segmentation.applyClassifier",inDir, list1[k], "showResults=true","storeResults=true", "probabilityMaps=true", outDir);
}
print("Done segmenting " + k + "images.");

