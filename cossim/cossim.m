%% Cosine Similarity taken in part form the following site
%% https://www.cs.purdue.edu/homes/dgleich/cs515/matlab/cosine_similarity.m
%%

% Load user data. Change argument as needed.
userdata = load('test5.txt');
userdoc = spconvert(userdata)';


% Load sparce Matrix of games
load matrix.txt;
Sm = spconvert(matrix);

% Normalize rows,
M = Sm';
n = size(M,2);
for i=1:n
    M(:,i) = M(:,1)/norm(M(:,i));
end

% nomalize user vector
userdoc = userdoc/norm(userdoc);
s = M'*userdoc;

[B, I] = sort(s, 'descend');

% Read in game name file
f = fopen('games.txt');             
g = textscan(f,'%s','delimiter','\n');
fclose(f);

% Read out the top 10 results
for n=1:10
    disp(g{1}{I(n)})
end