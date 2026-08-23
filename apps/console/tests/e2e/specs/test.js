// https://docs.cypress.io/api/introduction/api.html

describe('Soda risk console', () => {
  it('opens the engine playground', () => {
    cy.visit('/#/operations/playground')
    cy.url().should('include', '/#/operations/playground')
    cy.get('textarea').should('exist')
  })
})
