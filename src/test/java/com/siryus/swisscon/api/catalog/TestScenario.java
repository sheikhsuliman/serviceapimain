package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.dto.SnpAndId;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
class TestScenario {
    final AtomicInteger NODE_IDS = new AtomicInteger(0);
    final AtomicInteger VARIATION_IDS = new AtomicInteger(0);

    final Map<Integer, TestNode> ID_MAP = new HashMap<>();
    final Map<Integer, TestVariation> VARIATION_ID_MAP = new HashMap<>();

    final Map<String, List<TestNode>> ROOTS_MAP = new HashMap<>();
    final Map<String, List<TestNode>> SNP_MAP = new HashMap<>();
    final Map<String, List<TestNode>> PARENT_SNP_MAP = new HashMap<>();

    final Map<Integer, List<TestVariation>> VARIATIONS_MAP = new HashMap<>();

    TestNode root(String snp, String name) {
        return root(Constants.GLOBAL_CATALOG_COMPANY_ID, snp, name, false);
    }

    TestNode root(Integer companyId, String snp, String name) {
        return root(companyId, snp, name, false);
    }

    TestNode root(Integer companyId, String snp, String name, boolean disabled) {
        return new TestNode(this, companyId, null, snp, name, disabled);
    }

    static TestScenario scenario() {
        return new TestScenario();
    }

    public List<TestNode> roots(Integer companyId) {
        Map<String, TestNode> snpMap = new HashMap<>();

        ROOTS_MAP.values().stream().flatMap(List::stream)
                .filter(n -> n.companyId.equals(companyId))
                .forEach(n -> {
                    TestNode knownNode = snpMap.get(n.snp);
                    if (knownNode == null || knownNode.id < n.id) {
                        snpMap.put(n.snp, n);
                    }
                });
        return new ArrayList(snpMap.values());
    }

    public List<TestNode> nodes(List<Integer> ids) {
        return ID_MAP.values().stream().filter( n -> ids.contains(n.id)).collect(Collectors.toList());
    }

    public List<TestNode> children(Integer companyId, String parentSnp) {
        return filterOutOldNodes(
            PARENT_SNP_MAP.computeIfAbsent(parentSnp, k -> Collections.emptyList())
                .stream()
                .filter(n -> n.companyId.equals(companyId))
                .collect(Collectors.toList())
        );
    }

    public Optional<TestNode> latestWithSnp(Integer companyId, String snp) {
        return SNP_MAP.computeIfAbsent(snp, k -> Collections.emptyList())
                .stream().filter( n -> n.companyId.equals(companyId))
                .reduce( (a,b) -> a.id > b.id ? a : b);
    }

    public List<TestVariation> variations(Integer companyId, Integer nodeId) {

        return filterOutOldVariations(
            VARIATIONS_MAP.computeIfAbsent(nodeId, k -> Collections.emptyList()).stream()
                .filter( v -> v.companyId.equals(companyId))
                .collect(Collectors.toList())
        );
    }

    public List<TestNode> nodes(Integer companyId, String snp) {
        return SNP_MAP.get(snp).stream().filter( n -> n.companyId.equals(companyId)).collect(Collectors.toList());
    }

    private static Integer maxId(List<TestNode> nodes, String snp) {
        return nodes.stream()
                .filter(n -> n.snp.equals(snp))
                .reduce((a,b) -> a.id > b.id ? a : b)
                .map(n -> n.id)
                .orElse(null);
    }

    private List<TestNode> filterOutOldNodes(List<TestNode> nodes) {
        return nodes.stream()
                .filter( n -> n.id.equals(maxId(nodes, n.snp)))
                .collect(Collectors.toList());
    }

    private static Integer maxId(List<TestVariation> variations, Integer variationNumber) {
        return variations.stream()
                .filter(n -> n.variationNumber.equals(variationNumber))
                .reduce((a,b) -> a.id > b.id ? a : b)
                .map(n -> n.id)
                .orElse(null);
    }
    private List<TestVariation> filterOutOldVariations(List<TestVariation> variations) {
        return variations.stream()
                .filter( n -> n.id.equals(maxId(variations, n.variationNumber)))
                .collect(Collectors.toList());
    }


    public static class TestNode {
        final Integer id;
        final TestScenario scenario;
        final Integer companyId;
        final TestNode parent;
        final String snp;
        final String name;
        final boolean disabled;

        TestNode(TestScenario scenario, Integer companyId, TestNode parent, String snp, String name) {
            this(scenario, companyId, parent, snp, name, false);
        }

        TestNode(TestScenario scenario, Integer companyId, TestNode parent, String snp, String name, boolean disabled) {
            this.id = scenario.NODE_IDS.incrementAndGet();

            this.scenario = scenario;
            this.companyId = companyId;
            this.parent = parent;
            this.snp = snp;
            this.name = name;
            this.disabled = disabled;

            scenario.ID_MAP.put(this.id, this);
            scenario.SNP_MAP.computeIfAbsent(snp, k -> new ArrayList<>()).add(this);
            if (parent == null) {
                scenario.ROOTS_MAP.computeIfAbsent(snp, k -> new ArrayList<>()).add(this);
            } else {
                scenario.PARENT_SNP_MAP.computeIfAbsent(parent.snp, k -> new ArrayList<>()).add(this);
            }
        }

        TestNode child(String snp, String name) {
            return new TestNode(this.scenario, companyId, this, snp, name, disabled);
        }

        TestNode sibling(String snp, String name) {
            return new TestNode(this.scenario, companyId, this.parent, snp, name, disabled);
        }

        TestNode variation(Integer variationNumber, String taskName, String taskVariation) {
            return variation(this.companyId, variationNumber, taskName, taskVariation);
        }

        TestNode variation(Integer companyId, Integer variationNumber, String taskName, String taskVariation) {
            return variation(companyId, variationNumber, taskName, taskVariation, false, null);
        }

        TestNode variation(Integer companyId, Integer variationNumber, String taskName, String taskVariation, boolean active, BigDecimal price) {
            new TestVariation(scenario, companyId, this, variationNumber, taskName, taskVariation, active, price);
            return this;
        }

        TestNode parent() {
            return parent;
        }

        TestNode root() {
            return parent == null ? this : parent.root();
        }

        TestScenario scenario() { return scenario; }

        public static CatalogNodeEntity toEntity(TestNode testNode) {
            return CatalogNodeEntity.builder()
                    .id(testNode.id)
                    .companyId(testNode.companyId)
                    .name(testNode.name)
                    .snp(testNode.snp)
                    .disabled( testNode.disabled ? LocalDateTime.now() : null)
                .build();
        }

        public static SnpAndId toSnpAndId(TestNode testNode) {
            return new SnpAndId(testNode.snp, testNode.id);
        }
    }

    static class TestVariation {
        final Integer id;
        final TestScenario scenario;
        final Integer companyId;
        final TestNode node;
        final Integer variationNumber;
        final String taskName;
        final String taskVariation;
        final boolean active;
        final BigDecimal price;

        TestVariation(
                TestScenario scenario,
                Integer companyId,
                TestNode node,
                Integer variationNumber,
                String taskName,
                String taskVariation,
                boolean active,
                BigDecimal price) {
            this.id = scenario.VARIATION_IDS.incrementAndGet();

            this.scenario = scenario;
            this.companyId = companyId;
            this.node = node;
            this.variationNumber = variationNumber;
            this.taskName = taskName;
            this.taskVariation = taskVariation;
            this.active = active;
            this.price = price;

            scenario.VARIATION_ID_MAP.put(this.id, this);

            scenario.VARIATIONS_MAP.computeIfAbsent(node.id, k -> new ArrayList<>()).add(this);
        }

        public Integer getId() {
            return id;
        }

        public Integer getVariationNumber() {
            return variationNumber;
        }

        public static CatalogVariationEntity toEntity(TestVariation testVariation) {
            return CatalogVariationEntity.builder()
                    .id(testVariation.id)
                    .companyId(testVariation.companyId)
                    .catalogNodeId(testVariation.node.id)
                    .snp(testVariation.node.snp)
                    .variationNumber(testVariation.variationNumber)
                    .taskName(testVariation.taskName)
                    .taskVariation(testVariation.taskVariation)
                    .price(testVariation.price)
                    .build();
        }
    }
}
