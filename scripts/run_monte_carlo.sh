#!/bin/bash
#
# Monte Carlo Simulation Runner
# Usage: ./run_monte_carlo.sh [START_SEED] [END_SEED] [MODE]
#
# Examples:
#   ./run_monte_carlo.sh 1 5          # Run seeds 1-5 with full mode
#   ./run_monte_carlo.sh 1 10 light   # Run seeds 1-10 with light mode
#   ./run_monte_carlo.sh 3 3          # Run only seed 3
#

set -e

# Default values
START_SEED=${1:-1}
END_SEED=${2:-5}
MODE=${3:-full}

# Project paths
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
MODEL_DIR="$PROJECT_ROOT/benchmark/benchmark/Model"

# Select XML file based on mode
if [ "$MODE" = "light" ]; then
    XML_FILE="$MODEL_DIR/modelBenchmark_light.xml"
    GRADLE_TASK="runLight"
else
    XML_FILE="$MODEL_DIR/modelBenchmark_full.xml"
    GRADLE_TASK="runFull"
fi

# Validate XML file exists
if [ ! -f "$XML_FILE" ]; then
    echo "Error: XML file not found: $XML_FILE"
    exit 1
fi

# Function to update seed value in XML
update_seed() {
    local seed=$1
    local file=$2

    # Use sed to replace seed value
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s/<property name=\"seed\" value=\"[0-9]*\"/<property name=\"seed\" value=\"$seed\"/" "$file"
    else
        # Linux
        sed -i "s/<property name=\"seed\" value=\"[0-9]*\"/<property name=\"seed\" value=\"$seed\"/" "$file"
    fi
}

# Function to get current seed value
get_current_seed() {
    grep -o '<property name="seed" value="[0-9]*"' "$XML_FILE" | grep -o '[0-9]*'
}

# Print header
echo "========================================"
echo " Monte Carlo Simulation Runner"
echo "========================================"
echo " Seeds: $START_SEED to $END_SEED"
echo " Mode: $MODE"
echo " XML: $XML_FILE"
echo " Task: $GRADLE_TASK"
echo "========================================"
echo ""

# Store original seed to restore later
ORIGINAL_SEED=$(get_current_seed)
echo "Original seed value: $ORIGINAL_SEED"
echo ""

# Track results
SUCCESSFUL_RUNS=()
FAILED_RUNS=()

# Run simulations
for seed in $(seq $START_SEED $END_SEED); do
    echo "----------------------------------------"
    echo " Running seed: $seed / $END_SEED"
    echo "----------------------------------------"

    # Update seed in XML
    update_seed $seed "$XML_FILE"
    echo "Updated seed to: $(get_current_seed)"

    # Run Gradle task
    echo "Starting simulation..."
    START_TIME=$(date +%s)

    if (cd "$PROJECT_ROOT" && ./gradlew :benchmark:$GRADLE_TASK --no-daemon --quiet); then
        END_TIME=$(date +%s)
        DURATION=$((END_TIME - START_TIME))
        echo "Seed $seed completed successfully (${DURATION}s)"
        SUCCESSFUL_RUNS+=($seed)
    else
        echo "Seed $seed FAILED"
        FAILED_RUNS+=($seed)
    fi

    echo ""
done

# Restore original seed
update_seed $ORIGINAL_SEED "$XML_FILE"
echo "Restored original seed: $ORIGINAL_SEED"

# Print summary
echo ""
echo "========================================"
echo " Summary"
echo "========================================"
echo " Successful: ${#SUCCESSFUL_RUNS[@]} runs (seeds: ${SUCCESSFUL_RUNS[*]:-none})"
echo " Failed: ${#FAILED_RUNS[@]} runs (seeds: ${FAILED_RUNS[*]:-none})"
echo " Output: $PROJECT_ROOT/data/seed_*/"
echo "========================================"

# Exit with error if any run failed
if [ ${#FAILED_RUNS[@]} -gt 0 ]; then
    exit 1
fi
