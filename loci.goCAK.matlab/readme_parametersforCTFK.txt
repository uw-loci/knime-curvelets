ctfDEF.mat    % string, default parameter file or previous output data file 
C:\Users\yuming\git201502\knime-curvelets\loci.goCAK.matlab\testfiles  %string, image name
1B_D3_SHG_ROI_TACS3positive.tif-1.tif  %string, image path

postp = 0 % numeric, 0: process an image; 1: post-processing an image, dimensionless
%%fiber extraction parameters
pct = 0.2            %numeric, percentile of the remaining curvelet coeffs, dimensionless
SS = 3;              %numeric,number of the selected scales, dimensionless  
thresh_im2 = 5 % numeric, main adjustable parameters, unit:grayscale intensity  
xlinkbox = 8 % radius of box in which to check to make sure xlink is a local max of the distance function
thresh_ext = 70% numeric,, angle similarity required for a fiber to extend to the next point(cos(70*pi/180))
thresh_dang_L = 15; %numeric, dangler length threshold, in pixels
thresh_short_L = 15; %numeric, short fiber length threshold, in pixels
s_fiberdir  =4; % numeric, number of nodes used for calculating direction of fiber end, dimensionless,
thresh_linkd =  15 %numeric, distance for linking same-oriented fibers, dimensions
thresh_linka = 150 % numeric, minimum angle between two fiber ends for linking of the two fibers(cos(-150 *pi/180)), degree
thresh_flen = 15 %numeric, minimum length of a free fiber, pixels

%% parameters for width calculations

wid_opt = 1;     % numeric,choice for width calculation, 1: use all points; 0: use the following parameters;
wid_mm = 10;     % numeric,minimum maximum fiber width
wid_mp = 6;      % numeric,minimum points to apply fiber points selection
wid_sigma = 1;   %numeric, confidence region, default +- 1
wid_max = 0;     %numeric, calculate the maximum width of each fiber, deault 0, not calculate; 1: caculate

%% output image format control
LW1 = 0.5; % line width for the fibers displayed in the overlaid image, [dimensionless]
LL1 = 30;  % threshold of the fiber length, [pixels]
FNL = 99999;% maximum number of fibers in an individual image, [#]
RES = 300;   % image resolution of the overlaid image, [dpi]
widMAX = 15; % maximum width of any point on a fiber, [pixels]

% cvs output control
cP.angHV = 1;  %numeric, 1: output angle , 0: don't output angle
cP.lenHV = 1;  %numeric, 1: output length , 0: don't output length
cP.widHV = 1;  %numeric, 1: output width , 0: don't output width
cP.strHV = 1;  %numeric, 1: output straigthness , 0: don't output straightness
