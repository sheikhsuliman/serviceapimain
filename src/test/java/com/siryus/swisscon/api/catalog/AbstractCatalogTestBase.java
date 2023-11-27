package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.dto.SnpAndId;
import com.siryus.swisscon.api.catalog.dto.VariationNumberAndId;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeRepository;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationRepository;
import com.siryus.swisscon.api.general.unit.Unit;
import com.siryus.swisscon.api.general.unit.UnitService;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class AbstractCatalogTestBase {
    protected static final String TRADE_1_NAME = "Flat Roof";
    protected static final String TRADE_1_SNP = "100";
    protected static final String TRADE_2_NAME = "No Roof";
    protected static final String TRADE_2_SNP = "200";
    protected static final String TRADE_3_NAME = "Does Not Exist";
    protected static final String TRADE_3_SNP = "300";
    protected static final String GROUP_1_NAME = "Waterproofing membranes";
    protected static final String INVALID_SNP = "300";
    protected static final String GROUP_1_SNP = TRADE_1_SNP + ".100";
    protected static final String GROUP_2_NAME = "Waterproofing skins";
    protected static final String GROUP_2_SNP = TRADE_2_SNP + ".200";
    protected static final String VARIANT_1_NAME = "Bitumen sheets";
    protected static final String VARIANT_1_SNP = GROUP_1_SNP + ".100";
    protected static final String MAIN_TASK_1_NAME = "Undercoats";
    protected static final String MAIN_TASK_1_SNP = VARIANT_1_SNP + ".100";
    protected static final String TASK_1_NAME = MAIN_TASK_1_SNP + ".100";
    protected static final String TASK_1_SNP = MAIN_TASK_1_SNP + ".100";
    protected static final String TASK_2_NAME = MAIN_TASK_1_SNP + ".200";
    protected static final String TASK_2_SNP = MAIN_TASK_1_SNP + ".200";
    protected static final String VARIATION_TASK_NAME = "Task Name";
    protected static final String VARIATION_1_VARIATION = "A";
    protected static final String VARIATION_2_VARIATION = "B";
    protected static final String VARIATION_3_VARIATION = "C";
    protected static final String DELTA_1 = "-1";
    protected static final String M_2="m2";

    protected final List<CatalogNodeEntity> ROOT_NODES_LVL_1 = Arrays.asList(
            CatalogNodeEntity.builder().id(1).name(TRADE_1_NAME).snp(TRADE_1_SNP).build(),
            CatalogNodeEntity.builder().id(2).name(TRADE_2_NAME).snp(TRADE_2_SNP).build()
    );

    protected final List<CatalogNodeEntity> CHILDREN_NODES_LVL_2 = Arrays.asList(
            CatalogNodeEntity.builder().id(3).name(GROUP_1_NAME).snp(GROUP_1_SNP).parentSnp(TRADE_1_SNP).build(),
            CatalogNodeEntity.builder().id(4).name(GROUP_2_NAME).snp(GROUP_2_SNP).parentSnp(TRADE_2_SNP).build()
    );
    protected final List<CatalogNodeEntity> CHILDREN_NODES_LVL_3 = Arrays.asList(
            CatalogNodeEntity.builder().id(5).name(VARIANT_1_NAME).snp(VARIANT_1_SNP).parentSnp(GROUP_1_SNP).build()
    );
    protected final List<CatalogNodeEntity> CHILDREN_NODES_LVL_4 = Arrays.asList(
            CatalogNodeEntity.builder().id(6).name(MAIN_TASK_1_NAME).snp(MAIN_TASK_1_SNP).parentSnp(VARIANT_1_SNP).build()
    );
    protected final List<CatalogNodeEntity> CHILDREN_NODES_LVL_5 = Arrays.asList(
            CatalogNodeEntity.builder().id(7).name(TASK_1_NAME).snp(TASK_1_SNP).parentSnp(MAIN_TASK_1_SNP).build()
    );
    protected final List<CatalogVariationEntity> VARIATIONS = (List<CatalogVariationEntity>) Arrays.asList(
            CatalogVariationEntity.builder()
                    .id(1)
                    .catalogNodeId(CHILDREN_NODES_LVL_5.get(0).getId())
                    .snp(TASK_1_SNP)
                    .variationNumber(1)
                    .taskName(VARIATION_TASK_NAME)
                    .taskVariation(VARIATION_1_VARIATION)
                    .build(),
            CatalogVariationEntity.builder()
                    .id(2)
                    .catalogNodeId(CHILDREN_NODES_LVL_5.get(0).getId())
                    .snp(TASK_1_SNP)
                    .variationNumber(2)
                    .taskName(VARIATION_TASK_NAME)
                    .taskVariation(VARIATION_2_VARIATION)
                    .build()
    );
    protected final List<Integer> VARIATION_IDS = VARIATIONS.stream().map(CatalogVariationEntity::getId).collect(Collectors.toList());;

    protected final List<String> LEAF_NODE_SNP = CHILDREN_NODES_LVL_5.stream().map(CatalogNodeEntity::getSnp).collect(Collectors.toList());

    protected final List<Integer> ROOT_NODE_IDS = ROOT_NODES_LVL_1.stream().map(CatalogNodeEntity::getId).collect(Collectors.toList());

    protected final List<Integer> CHILDREN_NODE_LVL_2_IDS = CHILDREN_NODES_LVL_2.stream().map(CatalogNodeEntity::getId).collect(Collectors.toList());
    protected final List<Integer> CHILDREN_NODE_LVL_3_IDS = CHILDREN_NODES_LVL_3.stream().map(CatalogNodeEntity::getId).collect(Collectors.toList());
    protected final List<Integer> CHILDREN_NODE_LVL_4_IDS = CHILDREN_NODES_LVL_4.stream().map(CatalogNodeEntity::getId).collect(Collectors.toList());
    protected final List<Integer> CHILDREN_NODE_LVL_5_IDS = CHILDREN_NODES_LVL_5.stream().map(CatalogNodeEntity::getId).collect(Collectors.toList());

    protected final CatalogNodeRepository catalogNodeRepository = mock(CatalogNodeRepository.class);
    protected final CatalogVariationRepository variationRepository = mock(CatalogVariationRepository.class);

    protected final UnitService unitService = mock(UnitService.class);
    protected final Integer FIRST_NEXT_ID = 1000;
    protected AtomicInteger nextNodeId = new AtomicInteger(FIRST_NEXT_ID);
    private AtomicInteger nextVariationId = new AtomicInteger(FIRST_NEXT_ID);

    protected final Answer<CatalogNodeEntity> saveNodeAnswer = invocation ->
        ((CatalogNodeEntity)invocation.getArgument(0)).toBuilder()
                .id(nextNodeId.incrementAndGet())
            .build();

    protected final Answer<List<CatalogVariationEntity>> saveAllVariationsAnswer = invocation ->
        ((List<CatalogVariationEntity>)invocation.getArgument(0)).stream()
                .map( e -> e.toBuilder().id(nextVariationId.incrementAndGet()).build())
            .collect(Collectors.toList());

    void setupReposSaveAnswers() {
        nextNodeId.set(FIRST_NEXT_ID);

        when(catalogNodeRepository.save(any(CatalogNodeEntity.class))).then(saveNodeAnswer);
        when(variationRepository.saveAll(any())).then(saveAllVariationsAnswer);

        when(unitService.findBySymbolName(M_2)).thenReturn(
                Unit.builder()
                        .id(1)
                        .symbol(M_2)
                        .name(M_2)
                        .length(false)
                        .surface(true)
                   .build()
        );

    }

    TestScenario setupDefaultScenario() {
        return setupScenario(defaultScenario());
    }

    TestScenario setupScenario(TestScenario scenario) {
        Mockito.reset(catalogNodeRepository, variationRepository);

        when(catalogNodeRepository.listLatestIdsOfRootNodesForCompanyNotDisabled(anyInt())).then(
                new Answer<List<SnpAndId>>() {
                    @Override
                    public List<SnpAndId> answer(InvocationOnMock invocation) throws Throwable {
                        Integer companyId = invocation.getArgument(0);

                        return scenario.roots(companyId).stream()
                                .filter( n -> ! n.disabled)
                                .map(TestScenario.TestNode::toSnpAndId)
                                .collect(Collectors.toList());
                    }
                }
        );

        when(catalogNodeRepository.listLatestIdsOfChildNodesForCompanyNotDisabled(anyInt(), anyString())).then(
                new Answer<List<SnpAndId>>() {
                    @Override
                    public List<SnpAndId> answer(InvocationOnMock invocation) throws Throwable {
                        Integer companyId = invocation.getArgument(0);
                        String parentSnp = invocation.getArgument(1);

                        return scenario.children(companyId, parentSnp).stream()
                                .filter( n -> ! n.disabled)
                                .map(TestScenario.TestNode::toSnpAndId)
                                .collect(Collectors.toList());
                    }
                }
        );

        when(catalogNodeRepository.listLatestIdsOfRootNodesForCompany(anyInt())).then(
                new Answer<List<SnpAndId>>() {
                    @Override
                    public List<SnpAndId> answer(InvocationOnMock invocation) throws Throwable {
                        Integer companyId = invocation.getArgument(0);

                        return scenario.roots(companyId).stream()
                                .map(TestScenario.TestNode::toSnpAndId)
                                .collect(Collectors.toList());
                    }
                }
        );

        when(catalogNodeRepository.listLatestIdsOfChildNodesForCompany(anyInt(), anyString())).then(
                new Answer<List<SnpAndId>>() {
                    @Override
                    public List<SnpAndId> answer(InvocationOnMock invocation) throws Throwable {
                        Integer companyId = invocation.getArgument(0);
                        String parentSnp = invocation.getArgument(1);

                        return scenario.children(companyId, parentSnp).stream()
                                .map(TestScenario.TestNode::toSnpAndId)
                                .collect(Collectors.toList());
                    }
                }
        );

        when(catalogNodeRepository.findByIdInAndDisabledIsNull(anyList())).then(
                new Answer<List<CatalogNodeEntity>>() {
                    @Override
                    public List<CatalogNodeEntity> answer(InvocationOnMock invocation) throws Throwable {
                        List<Integer> ids = invocation.getArgument(0);

                        return scenario.nodes(ids).stream().map(TestScenario.TestNode::toEntity)
                                .filter(e -> e.getDisabled() == null)
                                .collect(Collectors.toList());
                    }
                }
        );

        when(catalogNodeRepository.findByIdIn(anyList())).then(
                new Answer<List<CatalogNodeEntity>>() {
                    @Override
                    public List<CatalogNodeEntity> answer(InvocationOnMock invocation) throws Throwable {
                        List<Integer> ids = invocation.getArgument(0);

                        return scenario.nodes(ids).stream().map(TestScenario.TestNode::toEntity).collect(Collectors.toList());
                    }
                }
        );

        when(catalogNodeRepository.findLatestWithSnp(anyInt(), anyString())).then(new Answer<Optional<CatalogNodeEntity> >() {
            @Override
            public Optional<CatalogNodeEntity> answer(InvocationOnMock invocation) throws Throwable {
                Integer companyId = invocation.getArgument(0);
                String snp = invocation.getArgument(1);

                return scenario.latestWithSnp(companyId, snp).map(TestScenario.TestNode::toEntity);
            }
        });

        when(variationRepository.listLatestVariations(anyInt(), anyInt())).then(new Answer<List<CatalogVariationEntity>>() {
            @Override
            public List<CatalogVariationEntity> answer(InvocationOnMock invocation) throws Throwable {
                Integer companyId = invocation.getArgument(0);
                Integer nodeId = invocation.getArgument(1);

                return scenario.variations(companyId, nodeId).stream()
                        .map(TestScenario.TestVariation::toEntity).collect(Collectors.toList());
            }
        });

        when(variationRepository.listLatestVariationsForCompanyAndGlobalCatalogNode(anyInt(), anyInt())).then(new Answer<List<VariationNumberAndId>>() {
            @Override
            public List<VariationNumberAndId> answer(InvocationOnMock invocation) throws Throwable {
                Integer companyId = invocation.getArgument(0);
                Integer nodeId = invocation.getArgument(1);

                return scenario.variations(companyId, nodeId).stream()
                        .map(v -> new VariationNumberAndId(v.variationNumber, v.id))
                        .collect(Collectors.toList());
            }
        });

        when(variationRepository.findAllById(anyList())).then(new Answer<List<CatalogVariationEntity>>() {
            @Override
            public List<CatalogVariationEntity> answer(InvocationOnMock invocation) throws Throwable {
                List<Integer> ids = invocation.getArgument(0);

                return scenario.VARIATION_ID_MAP.values().stream()
                        .filter( v -> ids.contains(v.id))
                        .sorted(Comparator.comparingInt(TestScenario.TestVariation::getVariationNumber))
                        .map(TestScenario.TestVariation::toEntity).collect(Collectors.toList());
            }
        });

        when(variationRepository.findVariation(anyInt(), anyInt(), anyInt())).then(
                (Answer<Optional<CatalogVariationEntity>>) invocation -> {
                    Integer companyId = invocation.getArgument(0);
                    Integer globalNodeId = invocation.getArgument(1);
                    Integer variationNumber = invocation.getArgument(2);

                    return scenario.VARIATION_ID_MAP.values().stream()
                            .filter(v -> v.companyId.equals(companyId) &&
                                    v.variationNumber.equals(variationNumber) &&
                                    v.node.id.equals(globalNodeId)).findFirst()
                            .map(TestScenario.TestVariation::toEntity);
                });

        when(variationRepository.countLatestVariations(anyInt(), anyInt())).then(
                (Answer<Integer>) invocation -> {
                    Integer companyId = invocation.getArgument(0);
                    Integer globalNodeId = invocation.getArgument(1);

                    return Math.toIntExact(scenario.VARIATION_ID_MAP.values().stream()
                            .filter(v -> v.companyId.equals(companyId) &&
                                    v.node.id.equals(globalNodeId)).count());
                });

        return scenario;
    }

    TestScenario defaultScenario() {
        return TestScenario.scenario()
            .root(TRADE_1_SNP, TRADE_1_NAME)
                .child(GROUP_1_SNP, GROUP_1_NAME)
                    .child(VARIANT_1_SNP, VARIANT_1_NAME)
                        .child(MAIN_TASK_1_SNP, MAIN_TASK_1_NAME)
                            .child(TASK_1_SNP, TASK_1_NAME)
                                .variation(1,  VARIATION_TASK_NAME, VARIATION_1_VARIATION)
                                .variation(2,  VARIATION_TASK_NAME, VARIATION_2_VARIATION)
            .root()
                .child(GROUP_2_SNP, GROUP_2_NAME)
        .scenario()
            .root(TRADE_2_SNP, TRADE_2_NAME)

        .scenario();
    }
}

