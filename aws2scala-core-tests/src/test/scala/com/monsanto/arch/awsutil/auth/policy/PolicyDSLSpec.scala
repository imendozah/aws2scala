package com.monsanto.arch.awsutil.auth.policy

import com.monsanto.arch.awsutil.auth.policy.PolicyDSL._
import com.monsanto.arch.awsutil.testkit.CoreScalaCheckImplicits._
import com.monsanto.arch.awsutil.testkit.UtilGen
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalatest.FreeSpec
import org.scalatest.Matchers._
import org.scalatest.prop.GeneratorDrivenPropertyChecks.forAll
import org.scalatest.prop.TableDrivenPropertyChecks.{Table, forAll ⇒ forAllIn}

class PolicyDSLSpec extends FreeSpec {
  TestAction.registerActions()

  "the policy DSL should has" - {
    "policy, which will" - {
      "build policies using" - {
        "only a statement list" in {
          forAll { statements: Seq[Statement] ⇒
            policy(statements) shouldBe Policy(Policy.Version.`2012-10-17`, None, statements)
          }
        }

        "an identifier and a statement list" in {
          forAll(arbitrary[Seq[Statement]], Gen.identifier) { (statements, policyId) ⇒
            policy(
              id(policyId),
              statements
            ) shouldBe Policy(Policy.Version.`2012-10-17`, Some(policyId), statements)
          }
        }

        "a statement list and an identifier" in {
          forAll(arbitrary[Seq[Statement]], Gen.identifier) { (statements, policyId) ⇒
            policy(
              statements,
              id(policyId)
            ) shouldBe Policy(Policy.Version.`2012-10-17`, Some(policyId), statements)
          }
        }

        "a version and a statement list" in {
          forAll { (statements: Seq[Statement], policyVersion: Policy.Version) ⇒
            policy(
              version(policyVersion),
              statements
            ) shouldBe Policy(policyVersion, None, statements)
          }
        }

        "a statement list and a version" in {
          forAll { (statements: Seq[Statement], policyVersion: Policy.Version) ⇒
            policy(
              statements,
              version(policyVersion)
            ) shouldBe Policy(policyVersion, None, statements)
          }
        }

        "a version, identifier, and statements" in {
          forAll(
            arbitrary[Policy.Version] → "version",
            Gen.identifier → "id",
            arbitrary[Seq[Statement]] → "statements"
          ) { (policyVersion, policyId, statements) ⇒
            policy (
              version(policyVersion),
              id(policyId),
              statements
            )
          }
        }

        "a version, statements, and identifier" in {
          forAll(
            arbitrary[Policy.Version] → "version",
            Gen.identifier → "id",
            arbitrary[Seq[Statement]] → "statements"
          ) { (policyVersion, policyId, statements) ⇒
            policy (
              version(policyVersion),
              statements,
              id(policyId)
            )
          }
        }

        "an identifier, version, and statements" in {
          forAll(
            arbitrary[Policy.Version] → "version",
            Gen.identifier → "id",
            arbitrary[Seq[Statement]] → "statements"
          ) { (policyVersion, policyId, statements) ⇒
            policy(
              id(policyId),
              version(policyVersion),
              statements
            )
          }
        }

        "an identifier, statements, and version" in {
          forAll(
            arbitrary[Policy.Version] → "version",
            Gen.identifier → "id",
            arbitrary[Seq[Statement]] → "statements"
          ) { (policyVersion, policyId, statements) ⇒
            policy(
              id(policyId),
              statements,
              version(policyVersion)
            )
          }
        }

        "statements, an identifier, and a version" in {
          forAll(
            arbitrary[Policy.Version] → "version",
            Gen.identifier → "id",
            arbitrary[Seq[Statement]] → "statements"
          ) { (policyVersion, policyId, statements) ⇒
            policy (
              statements,
              id(policyId),
              version(policyVersion)
            )
          }
        }

        "statements, a version, and an identifier" in {
          forAll(
            arbitrary[Policy.Version] → "version",
            Gen.identifier → "id",
            arbitrary[Seq[Statement]] → "statements"
          ) { (policyVersion, policyId, statements) ⇒
            policy (
              statements,
              version(policyVersion),
              id(policyId)
            )
          }
        }
      }

      "fail to build policies with" - {
        "an empty statement list" in {
          the [IllegalArgumentException] thrownBy {
            policy(Seq.empty[Statement])
          } should have message "The policy’s statement list may not be empty."
        }

        "two statement lists" in {
          forAll { (statements1: Seq[Statement], statements2: Seq[Statement]) ⇒
            the [IllegalArgumentException] thrownBy {
              policy(
                statements1,
                statements2
              )
            } should have message "A policy may only have one list of statements."
          }
        }

        "two identifiers" in {
          forAll(Gen.identifier, Gen.identifier) { (id1, id2) ⇒
            the [IllegalArgumentException] thrownBy {
              policy(
                id(id1),
                id(id2)
              )
            } should have message "A policy may only have one identifier."
          }
        }

        "two versions" in {
          forAll { (v1: Policy.Version, v2: Policy.Version) ⇒
            the [IllegalArgumentException] thrownBy {
              policy(
                version(v1),
                version(v2)
              )
            } should have message "A policy may only have one version."
          }
        }
      }
    }

    "version, which wraps a single Policy.Version" in {
      forAllIn(versions) { ver ⇒
        version(ver) shouldBe theSameInstanceAs (ver)
      }
    }

    "statements, which builds a statements sequence" - {
      "with a single statement" in {
        forAll { statement: Statement ⇒
          statements(statement) shouldBe Seq(statement)
        }
      }

      "with two statements" in {
        forAll { (s1: Statement, s2: Statement) ⇒
          statements(s1, s2) shouldBe Seq(s1, s2)
        }
      }

      "with three statements" in {
        forAll { (s1: Statement, s2: Statement, s3: Statement) ⇒
          statements(s1, s2, s3) shouldBe Seq(s1, s2, s3)
        }
      }

      "arbitrary numbers of statements" in {
        forAll { statementList: Seq[Statement] ⇒
          statements(statementList.head, statementList.tail: _*) shouldBe statementList
        }
      }
    }

    "id, which wraps a statement or policy ID" in {
      forAll(Gen.identifier) { identifier ⇒
        id(identifier).value shouldBe theSameInstanceAs (identifier)
      }
    }

    "principals, which returns a list of Principal objects" in {
      forAll { principalList: Seq[Principal] ⇒
        principals(principalList: _*) shouldBe principalList
      }
    }

    "actions, which returns a list of Action objects" in {
      forAll { actionList: Seq[Action] ⇒
        actions(actionList: _*) shouldBe actionList
      }
    }

    "conditions, which returns a list of Action objects" in {
      forAll { conditionList: Seq[Condition] ⇒
        conditions(conditionList: _*) shouldBe conditionList
      }
    }

    "allow, which will" - {
      "create policies" - {
        "with a sid argument" in {
          forAll(Gen.identifier) { identifier ⇒
            allow (
              id(identifier)
            ) shouldBe Statement(Some(identifier), Seq.empty, Statement.Effect.Allow, Seq.empty, Seq.empty, Seq.empty)
          }
        }

        "with a principals argument" in {
          forAll { principals: Seq[Principal] ⇒
            allow (
              principals
            ) shouldBe Statement(None, principals, Statement.Effect.Allow, Seq.empty, Seq.empty, Seq.empty)
          }
        }

        "with an actions argument" in {
          forAll { actions: Seq[Action] ⇒
            allow (
              actions
            ) shouldBe Statement(None, Seq.empty, Statement.Effect.Allow, actions, Seq.empty, Seq.empty)
          }
        }

        "with a resources argument" in {
          forAll { resources: Seq[Resource] ⇒
            allow (
              resources
            ) shouldBe Statement(None, Seq.empty, Statement.Effect.Allow, Seq.empty, resources, Seq.empty)
          }
        }

        "with a conditions argument" in {
          forAll { conditions: Seq[Condition] ⇒
            allow (
              conditions
            ) shouldBe Statement(None, Seq.empty, Statement.Effect.Allow, Seq.empty, Seq.empty, conditions)
          }
        }

        "with two arguments" in {
          forAll(
            arbitrary[Seq[Condition]] → "conditions",
            arbitrary[Seq[Action]] → "actions"
          ) { (conditions, actions) ⇒
            allow (
              conditions,
              actions
            ) shouldBe Statement(None, Seq.empty, Statement.Effect.Allow, actions, Seq.empty, conditions)
          }
        }

        "with three arguments" in {
          forAll(
            arbitrary[Seq[Condition]] → "conditions",
            arbitrary[Seq[Action]] → "actions",
            arbitrary[Seq[Principal]] → "principals"
          ) { (conditions, actions, principals) ⇒
            allow (
              conditions,
              actions,
              principals
            ) shouldBe Statement(None, principals, Statement.Effect.Allow, actions, Seq.empty, conditions)
          }
        }

        "with four arguments" in {
          forAll(
            arbitrary[Seq[Condition]] → "conditions",
            arbitrary[Seq[Action]] → "actions",
            arbitrary[Seq[Principal]] → "principals",
            Gen.identifier → "statementId"
          ) { (conditions, actions, principals, statementId) ⇒
            allow (
              conditions,
              actions,
              principals,
              id(statementId)
            ) shouldBe Statement(Some(statementId), principals, Statement.Effect.Allow, actions, Seq.empty, conditions)
          }
        }

        "with five arguments" in {
          forAll(
            arbitrary[Seq[Condition]] → "conditions",
            arbitrary[Seq[Action]] → "actions",
            arbitrary[Seq[Principal]] → "principals",
            Gen.identifier → "statementId",
            arbitrary[Seq[Resource]] → "resources"
          ) { (conditions, actions, principals, statementId, resources) ⇒
            allow (
              conditions,
              actions,
              principals,
              id(statementId),
              resources
            ) shouldBe Statement(Some(statementId), principals, Statement.Effect.Allow, actions, resources, conditions)
          }
        }
      }

      "fail to create policies with duplicated" - {
        "statement identifiers" in {
          forAll(Gen.identifier, Gen.identifier) { (id1, id2) ⇒
            the [IllegalArgumentException] thrownBy {
              allow (
                id(id1),
                id(id2)
              )
            } should have message "A statement may only have one identifier."
          }
        }

        "principal lists" in {
          forAll { (p1: Seq[Principal], p2: Seq[Principal]) ⇒
            the [IllegalArgumentException] thrownBy {
              allow ( p1, p2 )
            } should have message "A statement may only have one principal list."
          }
        }

        "action lists" in {
          forAll { (a1: Seq[Action], a2: Seq[Action]) ⇒
            the [IllegalArgumentException] thrownBy {
              allow ( a1, a2 )
            } should have message "A statement may only have one action list."
          }
        }

        "resource lists" in {
          forAll { (r1: Seq[Resource], r2: Seq[Resource]) ⇒
            the [IllegalArgumentException] thrownBy {
              allow ( r1, r2 )
            } should have message "A statement may only have one resource list."
          }
        }

        "condition lists" in {
          forAll { (c1: Seq[Condition], c2: Seq[Condition]) ⇒
            the [IllegalArgumentException] thrownBy {
              allow ( c1, c2 )
            } should have message "A statement may only have one condition list."
          }
        }
      }
    }

    "deny, which will" - {
      "create policies" - {
        "with a sid argument" in {
          forAll(Gen.identifier) { identifier ⇒
            deny (
              id(identifier)
            ) shouldBe Statement(Some(identifier), Seq.empty, Statement.Effect.Deny, Seq.empty, Seq.empty, Seq.empty)
          }
        }

        "with a principals argument" in {
          forAll { principals: Seq[Principal] ⇒
            deny (
              principals
            ) shouldBe Statement(None, principals, Statement.Effect.Deny, Seq.empty, Seq.empty, Seq.empty)
          }
        }

        "with an actions argument" in {
          forAll { actions: Seq[Action] ⇒
            deny (
              actions
            ) shouldBe Statement(None, Seq.empty, Statement.Effect.Deny, actions, Seq.empty, Seq.empty)
          }
        }

        "with a resources argument" in {
          forAll { resources: Seq[Resource] ⇒
            deny (
              resources
            ) shouldBe Statement(None, Seq.empty, Statement.Effect.Deny, Seq.empty, resources, Seq.empty)
          }
        }

        "with a conditions argument" in {
          forAll { conditions: Seq[Condition] ⇒
            deny (
              conditions
            ) shouldBe Statement(None, Seq.empty, Statement.Effect.Deny, Seq.empty, Seq.empty, conditions)
          }
        }

        "with two arguments" in {
          forAll(
            arbitrary[Seq[Condition]] → "conditions",
            arbitrary[Seq[Action]] → "actions"
          ) { (conditions, actions) ⇒
            deny (
              conditions,
              actions
            ) shouldBe Statement(None, Seq.empty, Statement.Effect.Deny, actions, Seq.empty, conditions)
          }
        }

        "with three arguments" in {
          forAll(
            arbitrary[Seq[Condition]] → "conditions",
            arbitrary[Seq[Action]] → "actions",
            arbitrary[Seq[Principal]] → "principals"
          ) { (conditions, actions, principals) ⇒
            deny (
              conditions,
              actions,
              principals
            ) shouldBe Statement(None, principals, Statement.Effect.Deny, actions, Seq.empty, conditions)
          }
        }

        "with four arguments" in {
          forAll(
            arbitrary[Seq[Condition]] → "conditions",
            arbitrary[Seq[Action]] → "actions",
            arbitrary[Seq[Principal]] → "principals",
            Gen.identifier → "statementId"
          ) { (conditions, actions, principals, statementId) ⇒
            deny (
              conditions,
              actions,
              principals,
              id(statementId)
            ) shouldBe Statement(Some(statementId), principals, Statement.Effect.Deny, actions, Seq.empty, conditions)
          }
        }

        "with five arguments" in {
          forAll(
            arbitrary[Seq[Condition]] → "conditions",
            arbitrary[Seq[Action]] → "actions",
            arbitrary[Seq[Principal]] → "principals",
            Gen.identifier → "statementId",
            arbitrary[Seq[Resource]] → "resources"
          ) { (conditions, actions, principals, statementId, resources) ⇒
            deny (
              conditions,
              actions,
              principals,
              id(statementId),
              resources
            ) shouldBe Statement(Some(statementId), principals, Statement.Effect.Deny, actions, resources, conditions)
          }
        }
      }

      "fail to create policies with duplicated" - {
        "statement identifiers" in {
          forAll(Gen.identifier, Gen.identifier) { (id1, id2) ⇒
            the [IllegalArgumentException] thrownBy {
              deny (
                id(id1),
                id(id2)
              )
            } should have message "A statement may only have one identifier."
          }
        }

        "principal lists" in {
          forAll { (p1: Seq[Principal], p2: Seq[Principal]) ⇒
            the [IllegalArgumentException] thrownBy {
              deny ( p1, p2 )
            } should have message "A statement may only have one principal list."
          }
        }

        "action lists" in {
          forAll { (a1: Seq[Action], a2: Seq[Action]) ⇒
            the [IllegalArgumentException] thrownBy {
              deny ( a1, a2 )
            } should have message "A statement may only have one action list."
          }
        }

        "resource lists" in {
          forAll { (r1: Seq[Resource], r2: Seq[Resource]) ⇒
            the [IllegalArgumentException] thrownBy {
              deny ( r1, r2 )
            } should have message "A statement may only have one resource list."
          }
        }

        "condition lists" in {
          forAll { (c1: Seq[Condition], c2: Seq[Condition]) ⇒
            the [IllegalArgumentException] thrownBy {
              deny ( c1, c2 )
            } should have message "A statement may only have one condition list."
          }
        }
      }
    }
  }

  private val versions = Table("version", Policy.Version.values: _*)

  implicit val arbStatements: Arbitrary[Seq[Statement]] =
    Arbitrary(UtilGen.nonEmptyListOfSqrtN(arbitrary[Statement]))

  implicit val shrinkStatements: Shrink[Seq[Statement]] =
    Shrink(statements ⇒ Shrink.shrinkContainer[Seq,Statement].shrink(statements).filter(_.nonEmpty))
}
