package org.deeplearning4j.zoo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.zoo.ModelMetaData;
import org.deeplearning4j.zoo.PretrainedType;
import org.deeplearning4j.zoo.ZooModel;
import org.deeplearning4j.zoo.ZooType;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.AdaDelta;
import org.nd4j.linalg.learning.config.IUpdater;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * U-Net
 *
 * Implementation of NASNet-A in Deeplearning4j. NASNet refers to Neural Architecture Search Network, a family of models
 * that were designed automatically by learning the model architectures directly on the dataset of interest.
 *
 * <p>This implementation uses 1056 penultimate filters and an input shape of (3, 224, 224). You can change this.</p>
 *
 * <p>Paper: https://arxiv.org/abs/1707.07012</p>
 * <p>ImageNet weights for this model are available and have been converted from https://keras.io/applications/.</p>
 *
 * @note If using the IMAGENETLARGE weights, the input shape is (3, 331, 331).
 * @author Justin Long (crockpotveggies)
 *
 */
@AllArgsConstructor
@Builder
public class NASNet extends ZooModel {

    @Builder.Default private long seed = 1234;
    @Builder.Default private int[] inputShape = new int[] {3, 224, 224};
    @Builder.Default private int penultimateFilters = 1056;
    private int numClasses;
    @Builder.Default private WeightInit weightInit = WeightInit.RELU;
    @Builder.Default private IUpdater updater = new AdaDelta();
    @Builder.Default private CacheMode cacheMode = CacheMode.DEVICE;
    @Builder.Default private WorkspaceMode workspaceMode = WorkspaceMode.ENABLED;
    @Builder.Default private ConvolutionLayer.AlgoMode cudnnAlgoMode = ConvolutionLayer.AlgoMode.PREFER_FASTEST;

    @Override
    public String pretrainedUrl(PretrainedType pretrainedType) {
        if (pretrainedType == PretrainedType.IMAGENET)
            return "http://blob.deeplearning4j.org/models/nasnetmobile_dl4j_inference.v1.zip";
        else if (pretrainedType == PretrainedType.IMAGENETLARGE)
            return "http://blob.deeplearning4j.org/models/nasnetlarge_dl4j_inference.v1.zip";
        else
            return null;
    }

    @Override
    public long pretrainedChecksum(PretrainedType pretrainedType) {
        if (pretrainedType == PretrainedType.IMAGENET)
            return 1654817155L;
        else if (pretrainedType == PretrainedType.IMAGENETLARGE)
            return 1654817155L;
        else
            return 0L;
    }

    @Override
    public Class<? extends Model> modelType() {
        return ComputationGraph.class;
    }

    @Override
    public ComputationGraph init() {
        ComputationGraphConfiguration.GraphBuilder graph = graphBuilder();

        graph.addInputs("input").setInputTypes(InputType.convolutional(inputShape[2], inputShape[1], inputShape[0]));

        ComputationGraphConfiguration conf = graph.build();
        ComputationGraph model = new ComputationGraph(conf);
        model.init();

        return model;
    }

    public ComputationGraphConfiguration.GraphBuilder graphBuilder() {

        ComputationGraphConfiguration.GraphBuilder graph = new NeuralNetConfiguration.Builder().seed(seed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(updater)
                .weightInit(weightInit)
                .dist(new NormalDistribution(0.0, 0.5))
                .l2(5e-5)
                .miniBatch(true)
                .cacheMode(cacheMode)
                .trainingWorkspaceMode(workspaceMode)
                .inferenceWorkspaceMode(workspaceMode)
                .cudnnAlgoMode(cudnnAlgoMode)
                .convolutionMode(ConvolutionMode.Truncate)
                .graphBuilder();


        graph
                // stem
                .addLayer("stem_conv1", new ConvolutionLayer.Builder(3,3).stride(2,2).nOut(penultimateFilters).hasBias(false)
                        .cudnnAlgoMode(cudnnAlgoMode).build(), "input")
                .addLayer("stem_bn1", new BatchNormalization(), "stem_conv1");

                // reduction


        graph
                // output
                .addLayer("act", new ActivationLayer(Activation.RELU), )
                .addLayer("avg_pool", new GlobalPoolingLayer.Builder(PoolingType.AVG).build(), "act")
                .addLayer("output", new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                    .activation(Activation.SOFTMAX).build(), "avg_pool")

                .setOutputs("output")
                .backprop(true)
                .pretrain(false);

        return graph;
    }

    @Override
    public ModelMetaData metaData() {
        return new ModelMetaData(new int[][] {inputShape}, 1, ZooType.CNN);
    }

    @Override
    public void setInputShape(int[][] inputShape) {
        this.inputShape = inputShape[0];
    }

}
