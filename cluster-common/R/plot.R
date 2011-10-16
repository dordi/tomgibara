#! /usr/bin/Rscript

args <- commandArgs(TRUE)
name <- args[1]
clusterCount <- as.numeric(args[2])


library(mclust)
points <- read.table(paste(name, ".txt", sep=""), header=FALSE, sep=" ")
plotWidth <- 400
plotHeight <- 400

png(paste(name, ".png", sep=""), width=plotWidth, height=plotHeight)
attach(points)
plot(V1,V2, main=name)
detach(points)
dev.off()

png(paste(name, "-gvm-clustered.png", sep=""), width=plotWidth, height=plotHeight)
pointsGvmClustered <- read.table(paste(name, "-clustered.txt", sep=""), header=FALSE, sep=" ")
attach(pointsGvmClustered)
plot(V1,V2,col=V3, main=paste("GVM clustered ", name, sep=""))
detach(pointsGvmClustered)
dev.off()

png(paste(name, "-kmeans-clustered.png", sep=""), width=plotWidth, height=plotHeight)
fit <- kmeans(points, clusterCount)
aggregate(points,by=list(fit$cluster),FUN=mean)
pointsKmeansClustered <- data.frame(points, fit$cluster)
attach(pointsKmeansClustered)
plot(V1,V2,col=pointsKmeansClustered$fit.cluster, main=paste("k-means clustered ", name, sep=""))
detach(pointsKmeansClustered)
dev.off()

png(paste(name, "-em-clustered.png", sep=""), width=plotWidth, height=plotHeight)
x = points[,1]
y = points[,2]
model <- Mclust(points,clusterCount)
plot(model, points, what="classification")
dev.off()

