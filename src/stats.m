PATH = "./data";

args = argv();
FILE = args{1};

data = csvread(fullfile(PATH, FILE));

format short g

disp("File"), disp(FILE)
disp("Moyenne "), disp(mean(data))
disp("Médiante "), disp(median(data))
disp("Variance "), disp(var(data))
disp("Ecart type"), disp(std(data))
